package com.jule.domino.dispacher.network.process;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.common.log.LoggerUtils;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.HttpsUtil;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.RoleType;
import com.jule.domino.base.model.GameRoomTableSeatRelationModel;
import com.jule.domino.base.model.UserTaskListModel;
import com.jule.domino.dispacher.network.protocol.Req;
import com.jule.domino.dispacher.service.LogService;
import com.jule.domino.dispacher.service.TaskService;
import com.jule.domino.dispacher.service.UserService;
import com.jule.domino.dispacher.service.VilidateService;
import com.jule.domino.dispacher.config.Config;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.network.DispacherFunctionFactory;
import com.jule.domino.dispacher.network.mail.MailServerGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class JoloCommon_LoginReq_600001 extends Req {
    private long time = 0;
    private byte[] blob;

    public JoloCommon_LoginReq_600001(int functionId) {
        super(functionId);
    }

    private JoloAuth.JoloCommon_LoginReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        time = System.currentTimeMillis();
        blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_LoginReq.parseFrom(blob);
    }

    @Override
    public void processImpl() throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            int vResult = VilidateService.OBJ.versionUpdate(req.getClientVersion(), req.getDownPlatform());
            if (vResult == 1) {
                DispacherFunctionFactory.getInstance().getResponse(DispacherFunctionFactory.__function__id_600006 | 0x08000000,
                        JoloAuth.JoloAuth_VersionAck.newBuilder().setResult(vResult).setDownLoadUrl(
                                VilidateService.OBJ.getDownPlatform(req.getDownPlatform())).build().toByteArray()).send(ctx, reqHeader);

                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                        JoloAuth.JoloCommon_LoginAck.newBuilder().setResult(0).setVerify("").
                                setResultMsg("Need to update, please click ok." + VilidateService.OBJ.getDownPlatform(req.getDownPlatform())
                                ).build().toByteArray()).send(ctx, reqHeader);
                return;
            }
            User user = DBUtil.selectUserByOpenId(req.getUserId());
            if (user == null) {
                user = DBUtil.selectByPrimaryKey(req.getUserId());
            }
            //黑名单禁止登陆
            if (VilidateService.OBJ.isBlackList(user)) {
                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                        JoloAuth.JoloCommon_LoginAck.newBuilder().setResult(-10).setVerify("").
                                setResultMsg("you are forbidden login").build().toByteArray()).send(ctx, reqHeader);
                return;
            }

            //非白名单
            if (!VilidateService.OBJ.isWhiteList(user)) {
                //维护时禁止登陆
                if (VilidateService.OBJ.isShutDown()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Date start = new Date(VilidateService.OBJ.getServer().getStartTime());
                    Date end = new Date(VilidateService.OBJ.getServer().getEndTime());
                    DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                            JoloAuth.JoloCommon_LoginAck.newBuilder().setResult(-10).setVerify("").
                                    setResultMsg("During game maintenance,\n time: [ " + dateFormat.format(start) + " - " + dateFormat.format(end) + " ]").build().toByteArray()).send(ctx, reqHeader);
                    return;
                }
            }

            byte[] ret = HttpsUtil.doPostProtoc(Config.LOGIN_URL, blob, Config.ENABLE_SSL);
            if (ret != null) {
                //记录user and link
                JoloAuth.JoloCommon_LoginAck ack = JoloAuth.JoloCommon_LoginAck.parseFrom(ret);
                log.info("ack - >" + (ack == null ? "" : ack.toString()));
                if (ack.getResult() == 1) {
                    //log.info("ack - >" + ack.toString());
                    UserService.getInstance().onUserLogin(ack.getUserId(), ctx);
                    if (!ack.getChannelId().equals(RoleType.ROBOT.getTypeName())) {
                        UserService.getInstance().onPlayerLogin(ack.getUserId());
                    }
                    //登录成功加载任务列表(可以异步去加载)
                    checkTaskList(ack.getUserId());
                    //登录成功加载邮件列表
                    checkMailList(ack.getUserId());
                }

                DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000, ret).send(ctx, reqHeader);

                //在桌内信息
                sb.append("inTable ");
                //首先查看自己有没有在游戏内
                GameRoomTableSeatRelationModel gameRoomTableSeatRelationModel = StoredObjManager.getStoredObjInMap(
                        GameRoomTableSeatRelationModel.class,
                        RedisConst.USER_TABLE_SEAT.getProfix(),
                        RedisConst.USER_TABLE_SEAT.getField() + ack.getUserId()
                );
                JoloAuth.JoloCommon_InTableAck.Builder inTableAck = JoloAuth.JoloCommon_InTableAck.newBuilder();
                if (gameRoomTableSeatRelationModel != null) {
                    String gameId = gameRoomTableSeatRelationModel.getGameId();
                    String roomId = gameRoomTableSeatRelationModel.getRoomId();
                    String tableId = gameRoomTableSeatRelationModel.getTableId();
                    int seatNum = gameRoomTableSeatRelationModel.getSeat();
                    sb.append("true,userId:" + ack.getUserId() + ",gameId:" + gameId + ",roomId:" + roomId + ",tableId:" + tableId + ",seatNum:" + seatNum);
                    inTableAck.setResult(1);
                    inTableAck.setUserId(ack.getUserId());
                    inTableAck.setGameId(gameId);
                    inTableAck.setRoomId(roomId);
                    inTableAck.setTableId(tableId);
                    inTableAck.setSeatNum(seatNum);
                    //发送玩家重连日志
                    LogService.OBJ.sendReconnectLog(user, new Date().getTime());
                } else {
                    sb.append("false");
                    inTableAck.setResult(0);
                }
                LoggerUtils.linkLog.info(sb.toString());
                DispacherFunctionFactory.getInstance().getResponse(
                        DispacherFunctionFactory.__function__id_600011 | 0x08000000,
                        inTableAck.build().toByteArray()).send(ctx, reqHeader);
            } else {
                log.warn("auth rpc fail! ->" + Config.LOGIN_URL);
            }
            long timeMillis = System.currentTimeMillis() - time;
            if (timeMillis > GameConst.COST_TIME) {
                LoggerUtils.performance.info("LoginReq_600001,cost time:{},userId:{}", timeMillis, req.getUserId());
            }
        } catch (Exception e) {
            DispacherFunctionFactory.getInstance().getResponse(functionId | 0x08000000,
                    JoloAuth.JoloCommon_LoginAck.newBuilder().setResult(0).setVerify("").
                            setResultMsg(ErrorCodeEnum.GAME_50002_2.getCode()).build().toByteArray()).send(ctx, reqHeader);
            log.error(e.getMessage(), e);
        }
    }

    private void checkTaskList(String userId) {
        UserTaskListModel model = StoredObjManager.hget(RedisConst.USER_TASK_LIST.getProfix(),
                RedisConst.USER_TASK_LIST.getField() + userId, UserTaskListModel.class);
        log.info("userId:{},taskList is null :{}", userId, model == null ? "yes" : "no");
        if (model != null) {
            boolean has = TaskService.getInstance().hasMatchCondition(model.getTaskMap());
            if (!has) {
                TaskService.getInstance().initTaskUserConfig(userId);
            }
        } else {//
            TaskService.getInstance().initTaskUserConfig(userId);
        }
    }

    private void checkMailList(String userId) {
        ChannelHandlerContext channelHandlerContext = null;
        ByteBuf byteBuf = null;
        try {
            channelHandlerContext = MailServerGroup.getInstance().getConnect();
            if (channelHandlerContext != null) {
                byteBuf = channelHandlerContext.alloc().buffer(28);
                byteBuf.writeInt(reqHeader.functionId);
                byteBuf.writeInt(reqHeader.gameId);
                byteBuf.writeInt(reqHeader.gameServerId);
                byteBuf.writeInt(reqHeader.isAsync ? 1 : 0);
                byteBuf.writeInt(reqHeader.reqNum);
                //临时写到预留字段里面标识是那个链接
                byteBuf.writeLong(Long.valueOf(userId));
                byteBuf.writeBytes(blob);
                LoggerUtils.mailLog.info("loginReq go to load mails ! " + reqHeader.toString());
                channelHandlerContext.writeAndFlush(byteBuf);
            } else {
                log.error("channelHandlerContext is null");
            }
        } catch (Exception e) {
            log.error("ReqHeader:{}," + System.getProperty("line.separator") + "Exception msg:{}", reqHeader.toString(), e.getMessage(), e);
        }
    }
}
