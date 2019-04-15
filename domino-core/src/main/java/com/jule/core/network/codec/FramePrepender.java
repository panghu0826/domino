package com.jule.core.network.codec;

import io.netty.handler.codec.LengthFieldPrepender;

import java.nio.ByteOrder;

public class FramePrepender extends LengthFieldPrepender {
    private FramePrepender(ByteOrder byteOrder, int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength) {
        super(byteOrder, lengthFieldLength, lengthAdjustment, lengthIncludesLengthFieldLength);

    }

    public FramePrepender(){
        this(ByteOrder.BIG_ENDIAN,4,0,false);
    }

}
