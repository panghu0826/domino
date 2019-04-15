package com.jule.domino.game.service;

import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.CardTypeMultipleModel;
import com.jule.domino.game.model.CardType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class CardTypeMultipleService {

    private Map<Integer, CardTypeMultipleModel> cardTypeMap = new HashMap<>();
    private int maxMultiple = 0;

    public CardTypeMultipleService() {
        discoverData();
        //2分钟执行一次destory()
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> discoverData(), 1, 120 * 1000, TimeUnit.MILLISECONDS
        );
    }

    /**
     * 服务监听启动后调用
     */
    public boolean discoverData() {
        List<CardTypeMultipleModel> cardTypes = DBUtil.getAllCardTypeMultiple();
        cardTypes.forEach(e -> {
            cardTypeMap.put(e.getCard_type(), e);
            if (e.getMultiple() > maxMultiple) {
                maxMultiple = e.getMultiple();
            }
        });
        return true;
    }

    public int getMultipleByCardType(int cardTypeValue) {
        try {
            CardTypeMultipleModel model = cardTypeMap.get(cardTypeValue);
            if (model == null) {
                log.error("getMultipleByCardType");
                return 1;
            }
            return model.getMultiple();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return 1;
    }

    private static class SingletonHolder {
        protected static final CardTypeMultipleService instance = new CardTypeMultipleService();
    }

    public static final CardTypeMultipleService getInstance() {
        return CardTypeMultipleService.SingletonHolder.instance;
    }

}
