package com.jule.core.jedis;

import com.jule.core.Main;
import org.junit.Assert;
import org.junit.Test;

public class StoredObjManagerTest {

    @Test
    public void testSetStoredObjInMap(){
        Main main = new Main();
        main.start();
        RoomInfo ri = new RoomInfo();
        TableInfo ti = new TableInfo();
        ti.setTableId(1);
        ri.getMap().put("10001",ti);
        boolean b = StoredObjManager.setStoredObjInMap(ri,"play_type_0","room_id_0");
        Assert.assertEquals(1,b?1:0);
    }
    @Test
    public void testGetStoredObjInMap(){
        Main main = new Main();
        main.start();
        RoomInfo ri = StoredObjManager.getStoredObjsInMap(RoomInfo.class,"play_type_0","room_id_0");
        Assert.assertEquals(1,ri.getMap().size());
        System.out.println(ri.getMap().get("10001").toString());
    }
}
