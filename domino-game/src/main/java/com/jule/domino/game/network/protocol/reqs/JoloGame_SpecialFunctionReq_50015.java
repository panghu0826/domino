package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.PlayerStateEnum;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.enums.TableStateEnum;
import com.jule.domino.game.gameUtil.GameLogic;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_ApplyFoldAck_50012;
import com.jule.domino.game.network.protocol.acks.JoloGame_SpecialFunctionAck_50015;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JoloGame_SpecialFunctionReq_50015 extends ClientReq {

    public JoloGame_SpecialFunctionReq_50015(int functionId) {
        super(functionId);
    }

    private JoloGame.JoloGame_SpecialFunctionReq req;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloGame.JoloGame_SpecialFunctionReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
    }

    @Override
    public void processImpl() throws Exception {
//        log.debug("收到消息, functionId->" + functionId + ", reqNum->" + header.reqNum + ", req->" + req.toString());
        log.info("收到消息, functionId->" + functionId +  ", req->" + req.toString());
        JoloGame.JoloGame_SpecialFunctionAck.Builder ack = JoloGame.JoloGame_SpecialFunctionAck.newBuilder();
        String userId = req.getUserId();
        ack.setUserId(userId);
        try {
            AbstractTable table = getTable();
            //记录桌子最后操作时间
            table.setLastActionTime(System.currentTimeMillis());
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            boolean flags = "vip".equals(user.getDevice_num());
            if (req.hasMaxHandCards()) {
                if(flags && table.getControlCardTypePlayerId() != null && !userId.equals(table.getControlCardTypePlayerId())){
                    log.info("vip不可顶掉当前已开特殊功能的人：MaxHandCards：{}，vipId：{}",table.getControlCardTypePlayerId(),userId);
                    ctx.writeAndFlush(new JoloGame_SpecialFunctionAck_50015(ack.setResult(-1).build(), header));
                    return;
                }
                table.setControlCardTypePlayerId(req.getMaxHandCards() == 1 ? userId : null);
            }
            if (req.hasSeeHandCards()) {
                if(flags && table.getSeeHandCardsPlayerId() != null && !userId.equals(table.getSeeHandCardsPlayerId())){
                    log.info("vip不可顶掉当前已开特殊功能的人：SeeHandCards：{}，vipId：{}",table.getSeeHandCardsPlayerId(),userId);
                    ctx.writeAndFlush(new JoloGame_SpecialFunctionAck_50015(ack.setResult(-1).build(), header));
                    return;
                }
                table.setSeeHandCardsPlayerId(req.getSeeHandCards() == 1 ? userId : null);
            }
            log.info("桌子目前开启特殊功能的玩家id：SeeHandCards：{}，MaxHandCards: {}",table.getSeeHandCardsPlayerId(),table.getControlCardTypePlayerId());
            ctx.writeAndFlush(new JoloGame_SpecialFunctionAck_50015(ack.setResult(1).build(), header));
        } catch (Exception ex) {
            ctx.writeAndFlush(new JoloGame_SpecialFunctionAck_50015(ack.setResult(-1).setResultMsg("该功能开启失败").build(), header));
            ex.printStackTrace();
        }finally {
            log.info("50015 ack 玩家开启特殊功能：{}", ack.toString());
        }
    }
}
