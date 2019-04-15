package com.jule.domino.gate.vavle.net;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

public class ChannelAttach {
	@Setter
    @Getter
	private ChannelHandlerContext channel;
	@Setter
    @Getter
	private long activityTime;

	public ChannelAttach(ChannelHandlerContext channel , long activityTime){
		this.channel = channel;
		this.activityTime = activityTime;
	}
}
