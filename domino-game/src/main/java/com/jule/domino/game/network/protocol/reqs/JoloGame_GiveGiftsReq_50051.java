package com.jule.domino.game.network.protocol.reqs;

import JoloProtobuf.GameSvr.JoloGame;
import com.jule.domino.base.enums.ErrorCodeEnum;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.acks.JoloGame_GiftsListAck_50051;
import com.jule.domino.game.network.protocol.acks.JoloGame_PlayRecordsAck_50063;
import com.jule.domino.game.service.PlayerRecordSerivce;
import com.jule.domino.game.service.TableService;
import com.jule.domino.game.vavle.notice.NoticeBroadcastMessages;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

/**
 * 牌局历史记录
 *
 * @author
 * @since 2018/9/11 18:55
 */
@Slf4j
public class JoloGame_GiveGiftsReq_50051 extends ClientReq {

    private JoloGame.JoloGame_GiftsListReq req;

    public JoloGame_GiveGiftsReq_50051(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf byteBuf) throws Exception {
        byte[] blob = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(blob);
        req = JoloGame.JoloGame_GiftsListReq.parseFrom(blob);
        this.setTable(TableService.getInstance().getTable(header.gameId + "", req.getRoomId(), req.getTableId()));
//        RabbitMqSender.me.producer(functionId,req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        log.info("收到消息functionId = {}, 消息体body={}", functionId, req.toString());
        try {
            ctx.writeAndFlush(new JoloGame_GiftsListAck_50051(
                    JoloGame.JoloGame_GiftsListAck.newBuilder()
                            .setUserId(req.getUserId())
                            .setTableId(req.getTableId())
                            .setRoomId(req.getRoomId())
                            .setResult(1).build(), header));
            NoticeBroadcastMessages.chatMesgSend(getTable(), req.getUserId(), req.getChatType(), req.getChatId(),req.getTargetId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
