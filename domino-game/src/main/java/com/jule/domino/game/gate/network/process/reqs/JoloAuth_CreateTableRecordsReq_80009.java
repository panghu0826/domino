package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.core.utils.MD5Security;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomCardRecordsModel;
import com.jule.domino.game.dao.bean.TableCreationRecordsModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 请求牌局记录
 */
public class JoloAuth_CreateTableRecordsReq_80009 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloAuth_CreateTableRecordsReq_80009.class);

    private JoloAuth.JoloAuth_CreateTableRecordsReq req;

    public JoloAuth_CreateTableRecordsReq_80009(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloAuth_CreateTableRecordsReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloAuth_CreateTableRecordsAck.Builder ack = JoloAuth.JoloAuth_CreateTableRecordsAck.newBuilder();
        try {
            List<TableCreationRecordsModel> list = DBUtil.selectTableCreateByUserId(req.getPlayerId());
            List<JoloAuth.JoloAuth_CreateTableRecords> array = new ArrayList<>();
            for(TableCreationRecordsModel tcrm : list){
                array.add(JoloAuth.JoloAuth_CreateTableRecords.newBuilder()
                        .setGameId(Integer.parseInt(tcrm.getGameId()))
                        .setTableId(tcrm.getTableId())
                        .setCreateTime(tcrm.getCreateTime())
                        .setPlayerNum(tcrm.getPlayerNum())
                        .setGameNum(tcrm.getGameNum()).build());
            }
            sendResponse(functionId | 0x08000000, ack.addAllCreateTableRecords(array).build().toByteArray());
        } catch (Exception e) {
        }
    }
}
