package com.jule.domino.auth.model;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;

public interface IAction {
    void handleGet(ChannelHandlerContext ctx, Map<String, List<String>> parameter, boolean isKeepAlive) throws Exception;

    void handlePost(ChannelHandlerContext ctx, byte[] payload, boolean isKeepAlive) throws Exception;

}
