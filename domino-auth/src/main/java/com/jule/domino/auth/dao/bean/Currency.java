package com.jule.domino.auth.dao.bean;

public class Currency {
    private Integer id;

    private String game_id;

    private String table_id;

    private String player_id;

    private String nick_name;

    private String operation;

    private double amount;

    private String game_order_id;

    private Long bet;

    private Long poundage;

    private Long win_jetton;

    private Long lose_jetton;

    private Long jetton;

    private double money;

    private double total_money;

    private String universal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id == null ? null : game_id.trim();
    }

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id == null ? null : table_id.trim();
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id == null ? null : player_id.trim();
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name == null ? null : nick_name.trim();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation == null ? null : operation.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getGame_order_id() {
        return game_order_id;
    }

    public void setGame_order_id(String game_order_id) {
        this.game_order_id = game_order_id == null ? null : game_order_id.trim();
    }

    public Long getBet() {
        return bet;
    }

    public void setBet(Long bet) {
        this.bet = bet;
    }

    public Long getPoundage() {
        return poundage;
    }

    public void setPoundage(Long poundage) {
        this.poundage = poundage;
    }

    public Long getWin_jetton() {
        return win_jetton;
    }

    public void setWin_jetton(Long win_jetton) {
        this.win_jetton = win_jetton;
    }

    public Long getLose_jetton() {
        return lose_jetton;
    }

    public void setLose_jetton(Long lose_jetton) {
        this.lose_jetton = lose_jetton;
    }

    public Long getJetton() {
        return jetton;
    }

    public void setJetton(Long jetton) {
        this.jetton = jetton;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getTotal_money() {
        return total_money;
    }

    public void setTotal_money(double total_money) {
        this.total_money = total_money;
    }

    public String getUniversal() {
        return universal;
    }

    public void setUniversal(String universal) {
        this.universal = universal == null ? null : universal.trim();
    }
}