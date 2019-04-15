package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import com.jule.domino.base.dao.bean.TaskUserHistModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter@Getter
public class UserTaskListModel extends StoredObj{
    /**
     * <id,TaskUserHistModel>
     */
    private Map<Integer,TaskUserHistModel> taskMap = new ConcurrentHashMap<>();

}
