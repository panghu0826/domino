package com.jule.domino.room.network;

import com.jule.domino.base.service.holder.FunctionIdsHolder;
import com.jule.domino.room.network.protocol.ClientReq;
import com.jule.domino.room.network.protocol.reqs.DummyReq_01;
import com.jule.domino.room.network.protocol.reqs.JoloRoom_ApplyChangeTableReq_40002;
import com.jule.domino.room.network.protocol.reqs.JoloRoom_ApplyJoinTableReq_40001;
import com.jule.domino.room.network.protocol.reqs.JoloRoom_ApplyJoinTableRobotReq_40003;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ClientPacketFactory {

    private static final ClientPacketHolder CLIENT_PACKET_HOLDER = new ClientPacketHolder();

    private static class SingletonHolder {
        protected static final ClientPacketFactory instance = new ClientPacketFactory();
    }

    public static final ClientPacketFactory getInstance() {
        return ClientPacketFactory.SingletonHolder.instance;
    }

    private ClientPacketFactory() {
        //新加消息要放到这里
        CLIENT_PACKET_HOLDER.addPacketPrototype(new DummyReq_01(01));
//        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_GetRoomListReq_40001(40001));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyChangeTableReq_40002(FunctionIdsHolder.Room_REQ_ChangeTable));
//        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_GetTableStatusInfoReq_40003(40003));
//        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyJoinTableReq_40004(40004));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyJoinTableReq_40001(40001));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyJoinTableRobotReq_40003(40003));

    }

    public ClientReq getClientReq(int functionId, ChannelHandlerContext channelHandlerContext) {

        return CLIENT_PACKET_HOLDER.getPacket(functionId, channelHandlerContext);
    }

    public static class ClientPacketHolder {

        /**
         * logger for this class
         */
        private static final Logger logger = LoggerFactory.getLogger(ClientPacketHolder.class);

        private Map<Integer, ClientReq> packetsPrototypes = new HashMap<Integer, ClientReq>();

        /**
         * @param req
         */
        public void addPacketPrototype(ClientReq req) {
            packetsPrototypes.put(req.getFunctionId(), req);
        }

        /**
         * @param functionId
         * @param channelHandlerContext
         * @return
         */
        private ClientReq getPacket(int functionId, ChannelHandlerContext channelHandlerContext) {
            ClientReq prototype = packetsPrototypes.get(functionId);

            if (prototype == null) {
                unknownPacket(functionId);
                return null;
            }

            ClientReq req = prototype.clone();
            req.setCtx(channelHandlerContext);

            return req;
        }

        /**
         * @param functionId
         */
        private void unknownPacket(int functionId) {
            logger.warn(String.format("Unknown packet recived from client: 0x%08X", functionId));
        }
    }
}
