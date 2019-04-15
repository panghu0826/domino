package com.jule.domino.base.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回的物品对象
 *
 * @author
 *
 * @since 2018/6/20 11:19
 *
 */
public class UnitVO {

    private List<ItemBean> items = new ArrayList<>();

    private int result;

    public UnitVO() {
    }

    public UnitVO( List<ItemBean> items, int result ) {
        this.items = items;
        this.result = result;
    }

    public List<ItemBean> getItems() {
        return items;
    }

    public void setItems( List<ItemBean> items ) {
        this.items = items;
    }

    public int getResult() {
        return result;
    }

    public void setResult( int result ) {
        this.result = result;
    }

}
