package com.jule.robot.util;

import com.jule.robot.model.CardValueModel;
import java.util.Arrays;
import java.util.Comparator;

public class StringTools {
//    public static String sort(String source){
//        char[] arrStr = source.toCharArray();
//        Arrays.sort(arrStr);
//        return new String(arrStr);
//    }

    public static String sortForPoker(String source){
        char[] arrChar = source.toCharArray();
        String[] arrStr = new String[arrChar.length];
        for(int i=0; i<arrChar.length; i++){
            arrStr[i] = arrChar[i]+"";
        }

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                CardValueModel model = new CardValueModel('♣', a.charAt(0));
                CardValueModel mode2 = new CardValueModel('♣', b.charAt(0));
                return model.compareTo(mode2) * -1; //乘-1，排列倒序
            }
        };

        Arrays.sort(arrStr, comparator);
        StringBuilder sb = new StringBuilder();
        for(String str : arrStr){
            sb.append(str);
        }
        return sb.toString();
    }

    public static String getDuplicateChar(String source){
        int length = source.length();
        for(int i=0;i<length;i++){
            if((source.indexOf(source.charAt(i), i+1))!=-1){
                return source.charAt(i)+"";
            }
        }
        return "";
    }
}
