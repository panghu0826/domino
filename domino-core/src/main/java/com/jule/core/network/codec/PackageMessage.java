package com.jule.core.network.codec;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

public class PackageMessage {
	
	//初始发送时间
    @Getter
    @Setter
	private long sendTime;
	
	//发送次数
    @Getter
    @Setter
	private AtomicLong sendCount;


	
	
}
