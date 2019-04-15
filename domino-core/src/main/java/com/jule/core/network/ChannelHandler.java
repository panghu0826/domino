package com.jule.core.network;

import io.netty.channel.ChannelHandlerContext;

public class ChannelHandler {
    /****
     * 生成sesseion ID
     *
     * @param ch
     * @return
     */
    public static long getSesseionId(ChannelHandlerContext ch) {
        //return MD5Util.toMD5String(ch.channel().hashCode() + "" + ch.channel().remoteAddress()).toLowerCase();
        return Long.valueOf(ch.channel().id().toString(), 16).longValue();
    }
}
