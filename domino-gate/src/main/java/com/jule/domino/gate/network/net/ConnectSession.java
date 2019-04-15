package com.jule.domino.gate.network.net;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

public class ConnectSession {
	@Setter
    @Getter
	private Long sesseionId;
	@Getter
	private String userId;
	
	@Getter
	private ChannelHandlerContext channel ;
	
	public ConnectSession(long sessionId,String userId) {
		this.sesseionId = sessionId;
		this.userId     = userId;
	}
	

	public ConnectSession setChannel(ChannelHandlerContext ch) {
		this.channel = ch;
		return this;
	}
}
