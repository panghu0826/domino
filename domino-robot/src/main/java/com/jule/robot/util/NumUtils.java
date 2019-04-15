package com.jule.robot.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NumUtils {
    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    /**
     * 格式化 保留两位小数
     * @param value
     * @return
     */
    public static double double2Decimal(double value){
        if (value == 0d){
            return value;
        }
        try {
            return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }catch (Exception e){
            log.error("double 格式化出现异常，返回原值，e = {}",e);
            return value;
        }
    }

    public static List<Integer> ConvertByte2IntArr(List<Byte> list){
        List<Integer> cards = new ArrayList<>(list.size());
        for (Byte b : list) {
            cards.add(NumUtils.byteToInt(b));
        }
        return cards;
    }

    public static List<Byte> ConvertInt2ByteArr(int[] list){
        List<Byte> cards = new ArrayList<>(list.length);
        for (int i = 0; i < list.length; i++) {
            cards.add(NumUtils.intToByte(list[i]));
        }
        return cards;
    }

    public static List<Byte> ConvertInt2ByteArr(List<Integer> list){
        List<Byte> cards = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            cards.add(NumUtils.intToByte(list.get(i)));
        }
        return cards;
    }
}
