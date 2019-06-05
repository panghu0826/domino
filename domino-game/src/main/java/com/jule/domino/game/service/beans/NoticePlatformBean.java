package com.jule.domino.game.service.beans;

import com.jule.domino.game.play.AbstractTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter@Getter
public class NoticePlatformBean {
    private double pfProfit = 0;
    private AbstractTable table;
    private List<NoticePlatformScoreBean> playerScoreList = new ArrayList<>();
}
