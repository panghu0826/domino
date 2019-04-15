package com.jule.core.network.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;

/**
 * Created by xujian on 2016/8/6.
 */
public class FrameDecoder extends LengthFieldBasedFrameDecoder {

    public FrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    public FrameDecoder() {
        this(ByteOrder.BIG_ENDIAN, 16384, 0, 4, 0, 4, false);
    }

}
