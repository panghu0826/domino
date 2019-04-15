package com.jule.domino.auth.utils;

import com.jule.domino.auth.dao.DBUtil;
import com.jule.domino.auth.dao.bean.Currency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static final ExecutorService es = Executors.newFixedThreadPool(1);

    public static int pool(Currency currency){
        currency.setGame_id("");
        currency.setTotal_money(currency.getMoney() + currency.getJetton());//玩家的总钱数(桌内+游戏大厅)
        es.submit(() -> {
            int count = DBUtil.insertCurrency(currency);
            return count;
        });
        return -1;
    }
}
