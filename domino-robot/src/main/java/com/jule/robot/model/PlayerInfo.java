package com.jule.robot.model;

import com.google.common.primitives.Ints;
import com.jule.robot.model.eenum.PlayTypeEnum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerInfo {
    private final static Logger logger = LoggerFactory.getLogger(PlayerInfo.class);

    private String userId;
    private String nickName;
    private int seatNum;
    private double playScoreStore;
    private int state;
    private int isBlind;
    private int mixedCard; //玩家手上有几张癞子牌
    private int[] handCards = null;//牌数组的下标
    private List<Integer> mixedCardList; //记录玩家结算后的混牌id

    public HandCardTypeModel getHandCardTypeModel(int gameId) {
        logger.debug("getHandCards = "+this.getHandCards().length + ", joker->"+PlayTypeEnum.Joker.getValue()+", hukam->"+PlayTypeEnum.hukam.getValue());
        //癞子牌玩法
        if (this.getHandCards() != null) {
            List<Integer> list = new ArrayList<>(Ints.asList(this.getHandCards()));
//            Iterator<Integer> it = list.iterator();
//            while (it.hasNext()) {
//                Integer ins = it.next();
//                if ((ins % 13) == (table.getMixedCardId() % 13)) {
//                    it.remove();
//                }
//            }
            logger.debug("list size->"+list.size());
            if (list.size() == 0) {
                this.setMixedCard(3);
                this.setHandCards(list.stream().mapToInt(i -> i).toArray());
            } else if (list.size() == 1) {
                this.setMixedCard(2);
                this.setHandCards(list.stream().mapToInt(i -> i).toArray());
            } else if (list.size() == 2) {
                this.setMixedCard(1);
                this.setHandCards(list.stream().mapToInt(i -> i).toArray());
            }
        }

        if (null != handCards) {
            if (handCards.length == 0) {//指定癞子牌时玩家可能会手握多张癞子
                return new HandCardTypeModel(this);
            } else if (handCards.length == 1) {
                return new HandCardTypeModel(this);
            } else if (handCards.length == 2) {
                return new HandCardTypeModel(this);
            } else if (handCards.length == 3) {
                return new HandCardTypeModel(handCards[0], handCards[1], handCards[2]);
            }
        }

        return null;
    }
}
