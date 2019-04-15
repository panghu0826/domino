package com.jule.domino.auth.dao.bean;

import lombok.Data;

import java.util.Date;

@Data
public class Payment {
    private Integer id;

    private String pid;

    private Double price;

    private String channel_statement;

    private String statement;

    private String app_id;

    private Date create_time;

    private Date update_time;

    private Integer state;

    private String sub_state;

    private String channel;

    private String syn_state;
    /**
     * 充值数量
     */
    private String reserved1 = "";

    private String reserved2 = "";

    private String reserved3 = "";
/**物品ID*/
    private String reserved4 = "";
/**money item*/
    private String reserved5 = "";

    private String reserved6 = "";

    private String reserved7 = "";

    private String reserved8 = "";

    private String reserved9 = "";

    public void setPid(String pid) {
        this.pid = pid == null ? null : pid.trim();
    }

    public void setChannel_statement(String channel_statement) {
        this.channel_statement = channel_statement == null ? null : channel_statement.trim();
    }

    public void setStatement(String statement) {
        this.statement = statement == null ? null : statement.trim();
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id == null ? null : app_id.trim();
    }

    public void setSub_state(String sub_state) {
        this.sub_state = sub_state == null ? null : sub_state.trim();
    }

    public void setChannel(String channel) {
        this.channel = channel == null ? null : channel.trim();
    }

    public void setSyn_state(String syn_state) {
        this.syn_state = syn_state == null ? null : syn_state.trim();
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1 == null ? null : reserved1.trim();
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2 == null ? null : reserved2.trim();
    }

    public void setReserved3(String reserved3) {
        this.reserved3 = reserved3 == null ? null : reserved3.trim();
    }

    public void setReserved4(String reserved4) {
        this.reserved4 = reserved4 == null ? null : reserved4.trim();
    }

    public void setReserved5(String reserved5) {
        this.reserved5 = reserved5 == null ? null : reserved5.trim();
    }

    public void setReserved6(String reserved6) {
        this.reserved6 = reserved6 == null ? null : reserved6.trim();
    }

    public void setReserved7(String reserved7) {
        this.reserved7 = reserved7 == null ? null : reserved7.trim();
    }

    public void setReserved8(String reserved8) {
        this.reserved8 = reserved8 == null ? null : reserved8.trim();
    }

    public void setReserved9(String reserved9) {
        this.reserved9 = reserved9 == null ? null : reserved9.trim();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", pid='" + pid + '\'' +
                ", price=" + price +
                ", channel_statement='" + channel_statement + '\'' +
                ", statement='" + statement + '\'' +
                ", app_id='" + app_id + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", state=" + state +
                ", sub_state='" + sub_state + '\'' +
                ", channel='" + channel + '\'' +
                ", syn_state='" + syn_state + '\'' +
                ", reserved1='" + reserved1 + '\'' +
                ", reserved2='" + reserved2 + '\'' +
                ", reserved3='" + reserved3 + '\'' +
                ", reserved4='" + reserved4 + '\'' +
                ", reserved5='" + reserved5 + '\'' +
                ", reserved6='" + reserved6 + '\'' +
                ", reserved7='" + reserved7 + '\'' +
                ", reserved8='" + reserved8 + '\'' +
                ", reserved9='" + reserved9 + '\'' +
                '}';
    }
}