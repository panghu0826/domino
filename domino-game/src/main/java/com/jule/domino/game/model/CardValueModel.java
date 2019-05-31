package com.jule.domino.game.model;

/**
 * 存储牌所对应的花色和数值
 */
public class CardValueModel {
    private char cardColor;
    private char cardValue;
    private int compareValue; //比较大小时使用的值
    private int cardId; //记录牌的下标，用于传给前端展现

    public CardValueModel(char cardColor, char cardValue){
        this.cardColor = cardColor;
        this.cardValue = cardValue;
        switch (cardValue){
            case 'A':
                this.compareValue = 7;
                break;
            case 'K':
                this.compareValue = 6;
                break;
            case 'Q':
                this.compareValue = 5;
                break;
            case 'J':
                this.compareValue = 4;
                break;
            case 'T':
                this.compareValue = 3;
                break;
            case '9':
                this.compareValue = 2;
                break;
            case '8':
                this.compareValue = 1;
                break;
        }
        if(this.cardColor == '♠'){
            this.cardId = this.compareValue;
        }else if(this.cardColor == '♥'){
            this.cardId = this.compareValue + 7;
        }else if(this.cardColor == '♣'){
            this.cardId = this.compareValue + 14;
        }else if(this.cardColor == '♦'){
            this.cardId = this.compareValue + 21;
        }
    }

    public char getCardColor() {
        return cardColor;
    }

    public char getCardValue() {
        return cardValue;
    }

    public int getCompareValue() {
        return compareValue;
    }

    public String toString(){
        return cardColor +""+ cardValue;
    }

    public int getCardId() {
        return cardId;
    }

}
