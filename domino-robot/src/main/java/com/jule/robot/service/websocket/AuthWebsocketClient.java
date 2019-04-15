package com.jule.robot.service.websocket;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.robot.config.Config;
import com.jule.robot.service.holder.FunctionIdHolder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class AuthWebsocketClient extends BaseWebSocketClient {
    private final static Logger logger = LoggerFactory.getLogger(AuthWebsocketClient.class);

    public AuthWebsocketClient(int gameId, String userId, String roomId, String tableId) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.tableId = tableId;
        this.userId = userId;
        //super.connectWebSocket(FunctionIdHolder.GATE_AUTH_SVR_URI);
    }

    @Override
    protected void routeFunctionId(int functionId, byte[] bytes) {
        switch (functionId) {
            case FunctionIdHolder.GATE_ACK_loginUser:
                AckLoginUser(functionId, bytes);
                return;
            default:
                logger.debug("UNKNOW functionID ->" + functionId + ", functionName ->" + FunctionIdHolder.GetFunctionName(functionId));
        }
    }

    @Override
    public void webSocketOpen() {
        if (StringUtils.isNotEmpty(userId)) {
            ExecuteLoginUser();
        }
    }

    /**
     * 如果userId不存在，那么会创建一个新的guest账号
     */
    public void ExecuteLoginUser() {
        JoloAuth.JoloCommon_LoginReq.Builder req = JoloAuth.JoloCommon_LoginReq.newBuilder();
        req.setUserId(userId);
        req.setToken(userId);
        req.setClientVersion("robot");
        req.setChannelId("robot");
        req.setUserIp("192.168.0.14");
        req.setPlatform(0);
        req.setPlatformVersion("1.0.0");
        req.setDeviceNum("abcdefg-gfedcba");
        req.setVerify("");

        int reqNum = (int) (System.currentTimeMillis() / 1000);
        logger.info("发送登录信息");
        sendData(FunctionIdHolder.GATE_REQ_loginUser, gameId, reqNum, req.build().toByteArray());
    }

    private void AckLoginUser(int functionId, byte[] bytes) {
        try {
            JoloAuth.JoloCommon_LoginAck ack = JoloAuth.JoloCommon_LoginAck.parseFrom(bytes);
            logger.debug("ACK Header, functionId->" + functionId + ", functionName->" + FunctionIdHolder.GetFunctionName(functionId) + ", ACK Body, result->" + ack.getResult() + ", ResultMsg->" + ack.getResultMsg() + ", userId->" + ack.getUserId()
                    + ", nickName->" + ack.getNickName() + ", money->" + ack.getMoney());

            if (ack.getResult() == 1) {
                logger.info("Login Success, userId=" + userId + ", nickName=" + ack.getNickName() +
                        ", money=" + ack.getMoney() + ", isNew=" + ack.getIsNew() + ", channelId=" +
                        ack.getChannelId() + ", verify=" + ack.getVerify() + ",roomId:" + roomId);
                if (Config.TEST_TYPE_IS_STRESS == 1) {
                    new RobotGameWebSocketClient(gameId, userId, roomId, tableId );
                } else {
                    if (StringUtils.isNotBlank(roomId)) {
                        new RobotGameWebSocketClient(gameId, userId, roomId, tableId );
                    } else {
                        logger.error("LoginSuccess. But roomId and tableId is null.");
                    }
                }
            } else {
                logger.error("Login Failed, userId:{}, result:{},resultMsg:{},money:{}",
                        userId,ack.getResult() ,ack.getResultMsg(),ack.getMoney());
            }
        } catch (Exception ex) {
            logger.error("AckLoginUser error, msg = " + ex.getMessage(), ex);
        } finally {
            /**
             * 注释by guoxu 20180802，由于auth服务做了修改（auth连接关闭时，会清除UserInfo的缓存信息，导致机器人坐下失败）。
             * 因此，机器人修改此处的关闭逻辑，认证成功后先不关闭auth连接，等机器人离桌后，同时关闭auth和game的连接。
             */
//            this.close();
        }
    }
}
