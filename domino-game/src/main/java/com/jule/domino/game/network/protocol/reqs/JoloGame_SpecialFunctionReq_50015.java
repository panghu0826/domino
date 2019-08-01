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
                table.getPlayer(userId).setControlCardType(req.getMaxHandCards() == 1 ? true : false);
                if(flags && table.getControlCardTypePlayerId() != null && !userId.equals(table.getControlCardTypePlayerId())){
                    log.info("vip不可顶掉当前已开特殊功能的人：MaxHandCards：{}，vipId：{}",table.getControlCardTypePlayerId(),userId);
                    NoticeBroadcastMessages.sendSpecialFunctionMsg(userId,table,"开启失败！该功能已有人使用。");
                    ctx.writeAndFlush(new JoloGame_SpecialFunctionAck_50015(ack.setResult(-1).build(), header));
                    return;
                }
                User u = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + table.getControlCardTypePlayerId(), User.class);
                if(u != null && "vip".equals(u.getDevice_num()) && !userId.equals(u.getId()) && table.getPlayer(u.getId()).isControlCardType()){
                    NoticeBroadcastMessages.sendSpecialFunctionMsg(u.getId(),table,"最大牌型功能已被别人使用");
                }
                table.setControlCardTypePlayerId(req.getMaxHandCards() == 1 ? userId : null);
            }
            if (req.hasSeeHandCards()) {
                table.getPlayer(userId).setSeeHandCards(req.getSeeHandCards() == 1 ? true : false);
                if(req.getSeeHandCards() == 1) {
                    table.getSeeHandCardsPlayerId().add(userId);
                }else {
                    table.getSeeHandCardsPlayerId().remove(userId);
                }
            }
            if(table.getControlCardTypePlayerId() == null){
                table.getInGamePlayersBySeatNum().forEach((k,v)->{
                    User player = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + v.getPlayerId(), User.class);
                    log.info("vip玩家判断是否开启特殊功能：{}，  {}","vip".equals(player.getDevice_num()),!player.getId().equals(userId));
                    if("vip".equals(player.getDevice_num()) && !player.getId().equals(userId)){
                        log.info("vip玩家自动开启特殊功能");
                        if(table.getPlayer(player.getId()).isControlCardType() && table.getControlCardTypePlayerId() == null) {
                            table.setControlCardTypePlayerId(player.getId());
                            NoticeBroadcastMessages.sendSpecialFunctionMsg(player.getId(),table,"最大牌型功能已开启");
                        }
                    }
                });
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
