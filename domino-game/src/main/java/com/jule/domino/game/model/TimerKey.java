package com.jule.domino.game.model;

import com.jule.domino.base.model.RoomTableRelationModel;

import java.util.Objects;

/**
 * @author xujian
 */
public final class TimerKey {
    public final long time;
    private RoomTableRelationModel roomTableRelation;

    public TimerKey(long time, RoomTableRelationModel model) {
        this.time = time;
        this.roomTableRelation = model;
    }

    public TimerKey(RoomTableRelationModel model) {
        this.roomTableRelation = model;
        this.time = 0;
    }

    public String getCompareCode(){
        //System.out.println("hashCode->"+roomTableRelation.getRoomId()+"_"+roomTableRelation.getTableId());
        return roomTableRelation.getGameId() + "_"+roomTableRelation.getRoomId()+"_"+roomTableRelation.getTableId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompareCode());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            //System.out.println("equals obj is null.");
            return false;
        }

        if(!(obj instanceof TimerKey)){
            //System.out.println("equals obj is not TimerKey class.");
            return false;
        }

        if(!getCompareCode().equals(((TimerKey) obj).getCompareCode())){
            //System.out.println("equals obj hashCode error. localCode->"+getCompareCode()+", objCode->"+((TimerKey) obj).getCompareCode());
            return false;
        }
        return true;
    }
}
