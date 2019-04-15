package com.jule.domino.game.model;

import lombok.Getter;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WinLoseRecord {
    @Getter
    private ConcurrentLinkedDeque<Integer> linkedDeque = new ConcurrentLinkedDeque<>();

    public void add(int result) {
        linkedDeque.addFirst(result);
        while (linkedDeque.size() > 5) {//并发会造成统计不正确
            linkedDeque.pollLast();
        }
    }
    public int won(){
        int result = 0;
        Iterator iter = linkedDeque.iterator();
        while(iter.hasNext()) {
            int value = (int)iter.next();
            if(value>0){
                result ++;
            }
        }
        return result;
    }
}
