package com.jule.domino.game.network;

import com.jule.domino.base.service.holder.FunctionIdsHolder;
import com.jule.domino.game.network.protocol.ClientReq;
import com.jule.domino.game.network.protocol.reqs.*;
import com.jule.domino.game.notice.network.protocol.reqs.JoloNotice_SendGamePlayMsgReq_10001;
import com.jule.domino.game.notice.network.protocol.reqs.JoloNotice_SendNormalMsgReq_10000;
import com.jule.domino.game.room.network.protocol.reqs.*;
import com.jule.domino.game.service.holder.FunctionIdHolder;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

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
        CLIENT_PACKET_HOLDER.addPacketPrototype(new GatePingReq_01(1));//网关心跳
        CLIENT_PACKET_HOLDER.addPacketPrototype(new GateUserLogoutReq_02(2));//通知服务器玩家下线



        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ApplySitDownReq_50001(FunctionIdHolder.Game_REQ_ApplySiteDown));//申请入座
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ApplyStandUpReq_50002(FunctionIdHolder.Game_REQ_ApplyStandUp));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ApplyLeaveReq_50003(FunctionIdHolder.Game_REQ_ApplyLeave));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ApplyBetReq_50005(FunctionIdHolder.Game_REQ_ApplyBet));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ReadyReq_50018(FunctionIdHolder.Game_REQ_ReadyType));

        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_OtherPlayerInfoReq_50014(FunctionIdHolder.Game_REQ_OtherUserInfo));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_ReconnectReq_50016(FunctionIdHolder.Game_REQ_Reconnect));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloGame_PlayRecordsReq_50063(FunctionIdHolder.Game_REQ_GameRecords));

        //新加room消息要放到这里
        CLIENT_PACKET_HOLDER.addPacketPrototype(new DummyReq_01(01));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyChangeTableReq_40002(FunctionIdsHolder.Room_REQ_ChangeTable));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyJoinTableReq_40001(40001));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_ApplyJoinTableRobotReq_40003(40003));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloRoom_RoomListReq_40005(40005));

        //新加notice消息要放到这里
        CLIENT_PACKET_HOLDER.addPacketPrototype(new com.jule.domino.game.notice.network.protocol.reqs.DummyReq_01(01));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloNotice_SendNormalMsgReq_10000(10000));
        CLIENT_PACKET_HOLDER.addPacketPrototype(new JoloNotice_SendGamePlayMsgReq_10001(10001));
    }

    public ClientReq getClientReq(int functionId, ChannelHandlerContext channelHandlerContext) {
//        logger.debug("getClientReq");
        return CLIENT_PACKET_HOLDER.getPacket(functionId, channelHandlerContext);
    }

    @Slf4j
    public static class ClientPacketHolder {

        private Map<Integer, ClientReq> packetsPrototypes = new HashMap<>();

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
//            logger.debug("getPacket");
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
            log.warn(String.format("Unknown packet recived from client: %s", functionId));
        }
    }
}
