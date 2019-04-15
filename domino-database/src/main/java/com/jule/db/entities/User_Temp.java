package com.jule.db.entities;

import com.jule.db.dao.BaseDbBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_temp")
public class User_Temp extends BaseDbBean {

    @Id
    @Column(name = "id")
    private  long uid;

    @Column
    private String name;

    @Override
    public Object getId() {
        return uid;
    }

    @Override
    public void setId( long id ) {
        this.uid = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid( long uid ) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
}
