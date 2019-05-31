package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.GameRecordModel;
import com.jule.domino.game.dao.bean.TableCreationRecordsModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求牌局记录
 */
public class JoloCommon_PlayRecordsReq_80007 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_PlayRecordsReq_80007.class);

    private JoloAuth.JoloCommon_PlayRecordsReq req;

    public JoloCommon_PlayRecordsReq_80007(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_PlayRecordsReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_PlayRecordsAck.Builder ack = JoloAuth.JoloCommon_PlayRecordsAck.newBuilder();
        String userId = req.getUserId();

        if (!req.hasTableId()) {
            List<GameRecordModel> list = DBUtil.selectByUserId(userId);
            List<JoloAuth.JoloAuth_UserGameRecords> userGameRecords = new ArrayList<>();
            for (GameRecordModel grm : list) {
                userGameRecords.add(JoloAuth.JoloAuth_UserGameRecords.newBuilder()
                        .setGameId(String.valueOf(grm.getGameId()))
                        .setTableId(grm.getTableId())
                        .setTotalWinLose(grm.getPlayerCurrScore())
                        .setTime(grm.getStartTime())
                        .setIsWinner(grm.getPlayerCurrScore() > 0 ? 1 : 2).build());
            }
            ack.addAllUserRecords(userGameRecords);
        } else {
            ack.addAllUserRecords(new ArrayList<>());
            TableCreationRecordsModel tcrm = DBUtil.selectTableCreateRecord(req.getTableId());
            JoloAuth.JoloAuth_TableGameParameter.Builder tgp = JoloAuth.JoloAuth_TableGameParameter.newBuilder();
            tgp.setTableId(tcrm.getTableId())
                    .setPlayerNum(tcrm.getPlayerNum())
                    .setBaseBetScore(tcrm.getBaseScore())
                    .setReadyCd(tcrm.getReadyCd())
                    .setBetCd(tcrm.getBetCd())
                    .setOpenCardCd(tcrm.getOpenCardCd())
                    .setBetMaxScore(tcrm.getBetMaxScore())
                    .setGameNum(tcrm.getGameNum())
                    .setIsWatch(tcrm.getIsWatch())
                    .setBetMultiple(tcrm.getBetMultiple());
            List<GameRecordModel> list = DBUtil.selectByTableId(tcrm.getTableId());
            List<JoloAuth.JoloAuth_TableGameRecords> tableGameRecords = new ArrayList<>();
            for (GameRecordModel grm : list) {
                tableGameRecords.add(JoloAuth.JoloAuth_TableGameRecords.newBuilder()
                        .setCurrGameNum(grm.getCurrGameNum())
                        .setUserId(grm.getUserId())
                        .setNickName(grm.getNickName())
                        .setCardType(grm.getCardType())
                        .setHandCards(grm.getHandCards())
                        .setBetScore(grm.getTotalTableScore())
                        .setWinLoseScore(grm.getWinLoseScore())
                        .setPlayerCurrScore(grm.getPlayerCurrScore())
                        .setIcoUrl(grm.getIcoUrl())
                        .setStartTime(grm.getStartTime())
                        .setEndTime(grm.getEndTime()).build());
            }
            tgp.addAllGameRecords(tableGameRecords);
            ack.setTableRecords(tgp);
        }
        sendResponse(functionId | 0x08000000, ack.setUserId(userId).setResult(1).build().toByteArray());
    }
}
