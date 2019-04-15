package com.jule.domino.auth.network.handler;


import com.jule.domino.auth.model.IAction;
import com.jule.domino.auth.service.DispacherService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;

/**
 * 路由Handler
 *
 * @author 郭君伟
 */
public class RouteHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //文件上传 文件下载 GET POST 请求

        if (!request.decoderResult().isSuccess()) {
            ctx.close();
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        String path = decoder.path();

        boolean isKeepAlive = HttpUtil.isKeepAlive(request);

        if (request.method() == GET) {
            //解析参数
            Map<String, List<String>> parameter = decoder.parameters();
            IAction action = DispacherService.getInstance().getAction(path);
            if (action == null) {
                ctx.close();
                return;
            }

            action.handleGet(ctx, parameter, isKeepAlive);
            return;
        }


        IAction action = DispacherService.getInstance().getAction(path);
        if (action == null) {
            ctx.close();
            return;
        }
        //解析参数
        byte[] protoBlob = new byte[request.content().readableBytes()];
        request.content().readBytes(protoBlob);
        action.handlePost(ctx, protoBlob, isKeepAlive);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //logger.debug(ctx.name(), cause);
        ctx.close();
    }

}
