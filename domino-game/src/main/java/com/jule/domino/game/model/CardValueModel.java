package com.jule.domino.game.model;

/**
 * 存储牌所对应的花色和数值
 */
public class CardValueModel {
    private char cardColor;
    private char cardValue;
    private int compareValue; //比较大小时使用的值
    private int corresponding; //对应牌的值用来设置癞子牌
    private int cardId; //记录牌的下标，用于传给前端展现

    public CardValueModel(char cardColor, char cardValue){
        this.cardColor = cardColor;
        this.cardValue = cardValue;
        switch (cardValue){
            case 'A':
                this.compareValue = 13;
                this.corresponding = 1;
                break;
            case 'K':
                this.compareValue = 12;
                this.corresponding = 13;
                break;
            case 'Q':
                this.compareValue = 11;
                this.corresponding = 12;
                break;
            case 'J':
                this.compareValue = 10;
                this.corresponding = 11;
                break;
            case 'T':
                this.compareValue = 9;
                this.corresponding = 10;
                break;
            case '9':
                this.compareValue = 8;
                this.corresponding = 9;
                break;
            case '8':
                this.compareValue = 7;
                this.corresponding = 8;
                break;
            case '7':
                this.compareValue = 6;
                this.corresponding = 7;
                break;
            case '6':
                this.compareValue = 5;
                this.corresponding = 6;
                break;
            case '5':
                this.compareValue = 4;
                this.corresponding = 5;
                break;
            case '4':
                this.compareValue = 3;
                this.corresponding = 4;
                break;
            case '3':
                this.compareValue = 2;
                this.corresponding = 3;
                break;
            case '2':
                this.compareValue = 1;
                this.corresponding = 2;
                break;
        }
        if(this.cardColor == '♠'){
            this.cardId = this.corresponding;
        }else if(this.cardColor == '♥'){
            this.cardId = this.corresponding + 13;
        }else if(this.cardColor == '♣'){
            this.cardId = this.corresponding + 26;
        }else if(this.cardColor == '♦'){
            this.cardId = this.corresponding + 39;
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

    public int getCorresponding() {
        return corresponding;
    }

    public String toString(){
        return cardColor +""+ cardValue;
    }

    public int getCardId() {
        return cardId;
    }

}
