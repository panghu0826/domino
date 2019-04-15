package com.jule.robot.util;

import java.util.Random;

public class RandomTools {
    /**
     * 有边界的随机数（0 ~ max）
     * @param maxNum
     * @return
     */
    public static int getRandomNum(int maxNum){
        Random random = new Random(System.currentTimeMillis()+System.nanoTime());
        return random.nextInt(maxNum)+1;
    }

    public static double getRandomDouble(double maxNum){
        double minNum = 0.01;
        double boundedDouble = minNum + new Random(System.currentTimeMillis()+System.nanoTime()).nextDouble() * (maxNum - minNum);
        return boundedDouble;
    }
}
