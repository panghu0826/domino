package com.jule.domino.game.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombineUtil {
    private static final Logger log = LoggerFactory.getLogger(CombineUtil.class);

    public static List<List<Byte>> combine(List<Byte> a, int num) {
        long oldTime = System.currentTimeMillis();
        List<List<Byte>> list = new ArrayList();
        Byte[] b = new Byte[a.size()];
        for (int i = 0; i < b.length; i++) {
            if (i < num) {
                b[i] = (byte) 1;
            } else {
                b[i] = (byte) 0;
            }
        }
        int point = 0;
        int nextPoint = 0;
        int count = 0;
        int sum = 0;
        byte temp = 1;
        while (true) {
            for (int i = b.length - 1; i >= b.length - num; i--) {
                try {
                    if (b[i].equals(Byte.valueOf((byte) 1))) {
                        sum++;
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            List<Byte> tmp = new ArrayList();
            for (int i = 0; i < b.length; i++) {
                if (b[i].equals(Byte.valueOf((byte) 1))) {
                    point = i;
                    tmp.add(a.get(point));
                    count++;
                    if (count == num) {
                        break;
                    }
                }
            }
            list.add(tmp);
            if (sum == num) {
                break;
            }
            sum = 0;
            for (int i = 0; i < b.length - 1; i++) {
                if ((b[i].equals(Byte.valueOf((byte) 1))) && (b[(i + 1)].equals(Byte.valueOf((byte) 0)))) {
                    point = i;
                    nextPoint = i + 1;
                    b[point] = Byte.valueOf((byte) 0);
                    b[nextPoint] = Byte.valueOf((byte) 1);
                    break;
                }
            }
            for (int i = 0; i < point - 1; i++) {
                for (int j = i; j < point - 1; j++) {
                    if (b[i].equals(Byte.valueOf((byte) 0))) {
                        temp = b[i].byteValue();
                        b[i] = b[(j + 1)];
                        b[(j + 1)] = Byte.valueOf(temp);
                    }
                }
            }
            count = 0;
        }
        long useTime = System.currentTimeMillis() - oldTime;
        if (useTime > 100) {
            log.error("use time: " + useTime);
        }
        return list;
    }
}