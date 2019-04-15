package com.jule.domino.auth.utils;

import com.jule.domino.auth.config.Config;
import com.jule.domino.auth.service.ProductionService;
import com.jule.domino.base.bean.ItemBean;
import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.bean.ItemType;
import com.jule.domino.base.bean.UnitVO;
import com.jule.domino.base.service.ItemServer;

import java.util.Iterator;

public class CheckUtils {
    public static ItemConfigBean checkHead(String headIco) {
        if( ProductionService.getInstance().getHeadMap().containsKey(headIco)){
            return ProductionService.getInstance().getHeadMap().get(headIco);
        }
        return null;
    }

    public static boolean expireHead(String userId,int itemId) {
        UnitVO unitVO = ItemServer.OBJ.getUnitByType(Config.GAME_ID, userId, ItemType.TYPE_ICO);
        if(unitVO.getResult()==0){
            Iterator<ItemBean> iterator = unitVO.getItems().iterator();
            while (iterator.hasNext()){
                ItemBean itemBean = iterator.next();
                if(itemBean.getItemID()==itemId){
                    return false;
                }
            }
        }
        return true;
    }
}
