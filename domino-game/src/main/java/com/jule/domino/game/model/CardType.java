package com.jule.domino.game.model;

import com.jule.domino.game.service.CardTypeMultipleService;

import java.util.List;

public enum CardType {

    wuxiaoniu("五小牛", 14),

    zhadanniu("炸弹牛", 13),

    wuhuaniu("五花牛", 12),

    sihuan("四花牛", 11),

    niuniu("牛牛", 10),

    niujiu("牛九", 9),

    niuba("牛八", 8),

    niuqi("牛七", 7),

    niuliu("牛六", 6),

    niuwu("牛五", 5),

    niusi("牛四", 4),

    niusan("牛三", 3),

    niuer("牛二", 2),

    niuyi("牛一", 1),

    wuniu("无牛", 0);

    private String name;

    private int type;

    private CardType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    /***
     * 比较牌型大小
     * @param cardType
     * @return
     */
    public int compare(CardType cardType) {
        if (this.type > cardType.getType()) {
            return 1;
        } else if (this.type < cardType.getType()) {
            return -1;
        } else {
            return 0;
        }
    }

    /***
     * 获取牌型倍数
     * @param wanfa
     * @return
     */
    public long getScore(long difen, List<String> wanfa, PlayerInfo bank,PlayerInfo nomal) {
        long score = 0;
        int multiple = 1;
        if (bank.getMultiple() > 1) {
            multiple = bank.getMultiple();
        }
        int carTypeMultiple = CardTypeMultipleService.getInstance().getMultipleByCardType(this.type);

        score = difen * carTypeMultiple * multiple * nomal.getBetMultiple();

        return score;
    }
}
