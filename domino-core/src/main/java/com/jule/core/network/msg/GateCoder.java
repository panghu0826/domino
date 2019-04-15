package com.jule.core.network.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.SwappedByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Gate服务 消息编码\解码器
 * @author
 * @since 2018/11/19 17:27
 */
@Slf4j
public class GateCoder {

    //单例
    public static final GateCoder OBJ = new GateCoder();

    /**
     * 消息编码
     * @param ctx
     * @param pack
     * @return
     */
    public ByteBuf encode(final ChannelHandlerContext ctx, MessagePack pack, boolean swap){
        try {
            //消息体长度
            int bodyLen = pack.getBody() == null ? 0 : pack.getBody().length;
            int len = bodyLen + 32;

            //写消息
            ByteBuf byteBuf = ctx.alloc().buffer(len);
            if (swap){
                SwappedByteBuf swapBuf = new SwappedByteBuf(byteBuf);
                swapBuf.writeInt(len);
                swapBuf.writeInt(pack.getFunctionId());
                swapBuf.writeInt(pack.getGameId());
                swapBuf.writeInt(pack.getGameSvrId());
                swapBuf.writeInt(pack.isAsync() ? 1 : 0);
                swapBuf.writeInt(pack.getReqNum());
                swapBuf.writeLong(pack.getChannelId());
                if (pack.getBody() != null){
                    swapBuf.writeBytes(pack.getBody());
                }

                return swapBuf;
            }else {
                byteBuf.writeInt(len);
                byteBuf.writeInt(pack.getFunctionId());
                byteBuf.writeInt(pack.getGameId());
                byteBuf.writeInt(pack.getGameSvrId());
                byteBuf.writeInt(pack.isAsync() ? 1 : 0);
                byteBuf.writeInt(pack.getReqNum());
                byteBuf.writeLong(pack.getChannelId());
                if (pack.getBody() != null){
                    byteBuf.writeBytes(pack.getBody());
                }

                return byteBuf;
            }

        }catch (Exception ex){
            log.error("消息编码失败 ex = ", ex);
        }
        return null;
    }

    /**
     * 消息解码-消息头
     * @param msg
     * @return
     */
    public MessagePack decodeHeader(ByteBuf msg, boolean swap){
        try {
            //消息接收对象
            MessagePack pack = new MessagePack();

            //读取消息头
            int length = 0;
            int msgId = 0;
            int gameId = 0;
            int gameSvrId = 0;
            boolean isAsync = false;
            int reqNum = 0;
            long channelId = 0l;

            if (swap){
                length = ByteBufUtil.swapInt(msg.readInt());
                msgId = ByteBufUtil.swapInt(msg.readInt());
                gameId = ByteBufUtil.swapInt(msg.readInt());
                gameSvrId = ByteBufUtil.swapInt(msg.readInt());
                isAsync = ByteBufUtil.swapInt(msg.readInt()) == 1;
                reqNum = ByteBufUtil.swapInt(msg.readInt());
                log.debug("length={},msgId={},gameId={},gameSvrId={},isAsync={},reqNum={}",length,msgId,gameId,gameSvrId,isAsync,reqNum);
                channelId = ByteBufUtil.swapLong(msg.readLong());
            }else {
                length = msg.readInt();
                msgId = msg.readInt();
                gameId = msg.readInt();
                gameSvrId = msg.readInt();
                isAsync = msg.readInt() == 1;
                reqNum = msg.readInt();
                log.debug("length={},msgId={},gameId={},gameSvrId={},isAsync={},reqNum={}",length,msgId,gameId,gameSvrId,isAsync,reqNum);
                channelId = msg.readLong();
            }

            //构建封包
            pack.setLength(length);
            pack.setFunctionId(msgId);
            pack.setGameId(gameId);
            pack.setGameSvrId(gameSvrId);
            pack.setAsync(isAsync);
            pack.setReqNum(reqNum);
            pack.setChannelId(channelId);

            return pack;
        }catch (Exception ex){
            log.error("解码消息头异常 ex = ", ex);
        }
        return null;
    }

    /**
     * @param msg
     * @param isOpen
     * @param STR
     * @param index  启始位置
     */
    public void encryptionAndDecryption(ByteBuf msg, boolean isOpen, byte[] STR, int index) {
        if (isOpen) {
            int len = msg.readableBytes() - index > STR.length ? STR.length : msg.readableBytes();
            log.debug("readableBytes:{},len:{}",msg.readableBytes(),len);
            for (int i = 0; i < len; i++) {
                msg.setByte(i, msg.getByte(i) ^ STR[i]);
            }
        }
    }

}
