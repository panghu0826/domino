package com.jule.domino.dispacher.dao.bean;

public class AdvertisingConfigModel {
    private Integer id;

    private Long establishl_time;

    private String address;

    private Long create_time;

    private Long expire_time;

    private String jump_link;

    private Byte advert_switch;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getEstablishl_time() {
        return establishl_time;
    }

    public void setEstablishl_time(Long establishl_time) {
        this.establishl_time = establishl_time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Long create_time) {
        this.create_time = create_time;
    }

    public Long getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(Long expire_time) {
        this.expire_time = expire_time;
    }

    public String getJump_link() {
        return jump_link;
    }

    public void setJump_link(String jump_link) {
        this.jump_link = jump_link == null ? null : jump_link.trim();
    }

    public Byte getAdvert_switch() {
        return advert_switch;
    }

    public void setAdvert_switch(Byte advert_switch) {
        this.advert_switch = advert_switch;
    }
}