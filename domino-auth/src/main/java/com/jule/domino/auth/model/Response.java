package com.jule.domino.auth.model;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Response {

	    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	    private static final AsciiString CONNECTION = new AsciiString("Connection");
	    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
	
	public static FullHttpResponse build(byte[] date, boolean iskeepAlive){
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(date));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        if (iskeepAlive) {
        	 response.headers().set(CONNECTION, KEEP_ALIVE);
         }
        return response;
	}

	/**
	 * 构建失败请求
	 * @return
	 */
	public static FullHttpResponse build400(){
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, Unpooled.wrappedBuffer(new byte[0]));
		response.headers().set(CONTENT_TYPE, "text/plain");
		response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
		return response;
	}
	
}
