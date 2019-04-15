package com.jule.domino.game.gw.netty;


import com.jule.core.network.ChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.SwappedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @since 2018/11/22 16:12
 */
@Slf4j@Getter
public class GwcMsgSerivce {

    public static final GwcMsgSerivce OBJ = new GwcMsgSerivce();

    //消息处理器
    private Map<Integer , AbstractGwcHander> _msgHandler = new HashMap<>();

    private Map<Long , ChannelHandlerContext> _ioHandler = new HashMap<>();

    /**
     * 注册消息 处理器
     * @param cmd
     * @param handler
     */
    public void regHandler(int cmd, AbstractGwcHander handler){
        _msgHandler.put(cmd, handler);
    }

    public void regIoHandler(ChannelHandlerContext ctx){
        _ioHandler.put(ChannelHandler.getSesseionId(ctx), ctx);
    }

    public void removeIoHandler(ChannelHandlerContext ctx){
        long session = ChannelHandler.getSesseionId(ctx);
        if (_ioHandler.containsKey(session)){
            _ioHandler.remove(session);
        }
    }

    /**
     * 获取消息处理器
     * @param cmd
     * @return
     */
    public AbstractGwcHander getHandler(int cmd){
        if (_msgHandler.containsKey(cmd)){
            return _msgHandler.get(cmd);
        }
        return null;
    }

    /**
     * 消息编码并发送
     * @param ctx
     * @param msgId
     * @param body
     */
    public void encode(ChannelHandlerContext ctx, int msgId, byte[] body){
        //写字节
        ByteBuf msg = ctx.alloc().buffer(1024);
        try {
            int length = body.length + 8;
            //大端转小端 (java 默认大端、合作方约定小端、所以需要这么干)
            SwappedByteBuf swapBuf = new SwappedByteBuf(msg);

            swapBuf.writeInt(length);
            swapBuf.writeInt(msgId);
            swapBuf.writeBytes(body);

            ctx.writeAndFlush(msg);
            log.debug("响应消息cmd = {}, msg = {}", msgId, msg.toString());
        }catch (Exception ex){
            log.error("发送消息异常", ex);
        }finally {
            //ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 消息编码并发送
     * @param msgId
     * @param body
     */
    public void encode(int msgId, byte[] body){
        int length = body.length + 8;

        //写字节
        ByteBuf msg = Unpooled.buffer(length);
        try {
            ChannelHandlerContext ctx = this.getCtx();
            if (ctx == null){
                log.error("无可用通道发送消息");
                return;
            }
            //大端转小端 (java 默认大端、合作方约定小端、所以需要这么干)
            SwappedByteBuf swapBuf = new SwappedByteBuf(msg);

            swapBuf.writeInt(length);
            swapBuf.writeInt(msgId);
            swapBuf.writeBytes(body);

            ctx.writeAndFlush(swapBuf);
            log.debug("响应消息cmd = {}, msg = {}", msgId, swapBuf.toString());
        }catch (Exception ex){
            log.error("发送消息异常", ex);
        }finally {
            //ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 消息接收并解码
     * @param msg
     * @return
     */
    public GwcMsg decode(Object msg){
        ByteBuf buf = (ByteBuf) msg;
        try {
            //大端转小端
            SwappedByteBuf swapBuf = new SwappedByteBuf(buf);

            //读取消息
            int length = swapBuf.readInt();
            int cmd = swapBuf.readInt();
            byte[] data = new byte[swapBuf.readableBytes()];
            swapBuf.readBytes(data);

            //构造消息对象
            GwcMsg gwcMsg = new GwcMsg();
            gwcMsg.setLen(length);
            gwcMsg.setCmd(cmd);
            gwcMsg.setBody(data);
            return gwcMsg;
        }catch (Exception ex){
            log.error("消息解码异常",ex);
        }finally {
            ReferenceCountUtil.release(msg);
        }
        return null;
    }

    private ChannelHandlerContext getCtx (){
        if (_ioHandler != null && _ioHandler.size() != 0){
            for (long sessionId :_ioHandler.keySet()){
                ChannelHandlerContext ctx = _ioHandler.get(sessionId);
                if (ctx.channel().isActive()){
                    return ctx;
                }
            }
        }
        return null;
    }

}
