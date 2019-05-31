package com.jule.domino.game.gate.network.process.reqs;

import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.gate.pool.game.GameServerGroup;
import com.jule.domino.game.gate.service.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xujian 2017-12-29
 */
@Slf4j
public class GameProxyReq extends Req {
    public GameProxyReq(int functionId) {
        super(functionId);
    }

    private byte[] bytes;

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
    }

    @Override
    public void processImpl() throws Exception {
        ChannelHandlerContext channelHandlerContext = null;
        ByteBuf byteBuf = null;
        try {
            channelHandlerContext = GameServerGroup.getInstance().getConnect(reqHeader.gameServerId);
            if (channelHandlerContext != null) {
                byteBuf = channelHandlerContext.alloc().buffer(bytes.length + 28);
                byteBuf.writeInt(reqHeader.functionId);
                byteBuf.writeInt(reqHeader.gameId);
                byteBuf.writeInt(reqHeader.gameServerId);
                byteBuf.writeInt(reqHeader.isAsync ? 1 : 0);
                byteBuf.writeInt(reqHeader.reqNum);
                //临时写到预留字段里面标识是那个链接
                byteBuf.writeLong(Long.valueOf(ctx.channel().id().toString(), 16));
                byteBuf.writeBytes(bytes);
                //在当前连接上记录最近交互的游戏服务器id
                ctx.channel().attr(UserService.LAST_GAMESVR_ID).set(reqHeader.gameServerId);
                channelHandlerContext.writeAndFlush(byteBuf);
            } else {
                log.error("channelHandlerContext is null");
            }
        } catch (Exception e) {
            channelHandlerContext = GameServerGroup.getInstance().getConnect(reqHeader.gameServerId);
            if (channelHandlerContext != null && byteBuf != null) {
                channelHandlerContext.writeAndFlush(byteBuf);
            }
            log.error("ReqHeader:{}," + System.getProperty("line.separator") + "Exception msg:{}", reqHeader.toString(), e.getMessage(), e);
        }

    }
}
