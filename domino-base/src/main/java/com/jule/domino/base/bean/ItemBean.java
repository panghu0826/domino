package com.jule.domino.base.bean;


import java.io.Serializable;

/**
 * 物品单元
 *
 * @author
 *
 * @since 2018/8/1 17:03
 *
 */
public class ItemBean implements Serializable{

    //物品唯一ID
    private long itemUUID;
    //物品配置ID
    private int itemID;
    //物品数量
    private int itemNum;
    //剩余时间
    private long timeOut;

    public ItemBean() {
    }

    public ItemBean(int itemID, int itemNum ) {
        this.itemID = itemID;
        this.itemNum = itemNum;
    }

    public ItemBean(long itemUUID, int itemID, int itemNum ) {
        this.itemUUID = itemUUID;
        this.itemID = itemID;
        this.itemNum = itemNum;
    }

    public long getItemUUID() {
        return itemUUID;
    }

    public void setItemUUID(long itemUUID) {
        this.itemUUID = itemUUID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum( int itemNum ) {
        this.itemNum = itemNum;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
