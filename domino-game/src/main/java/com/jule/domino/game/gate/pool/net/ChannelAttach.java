package com.jule.domino.game.gate.pool.net;

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
