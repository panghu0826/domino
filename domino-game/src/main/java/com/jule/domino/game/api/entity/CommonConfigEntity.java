package com.jule.domino.game.api.entity;

import com.jule.domino.game.dao.bean.CommonConfigModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 
 * </p>
 *
 * @author panghu
 * @since 2018-11-01
 */
@Getter
@Setter
@ToString
public class CommonConfigEntity {

    private static final long serialVersionUID = 1L;

    public CommonConfigEntity(CommonConfigModel configModel) {
        this.id = configModel.getId();
        this.betCD = configModel.getBetCountDownSec();
        this.gameStartCD = configModel.getGameStartCountDownSec();
        this.openCardsCD = configModel.getOpenCardsCD();
        this.settleCD = configModel.getSettleCD();
    }

    /**
     * 配置表序号
     */
    private int id;
    /**
     * 下注倒计时
     */
    private int betCD;
    /**
     * 游戏开始倒计时（发牌前倒计时）
     */
    private int gameStartCD;
    /**开牌倒计时*/
    private int openCardsCD;
    /**结算特效倒计时*/
    private int settleCD;
}
