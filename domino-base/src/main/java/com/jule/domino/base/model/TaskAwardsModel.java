package com.jule.domino.base.model;

import com.jule.core.jedis.StoredObj;
import com.jule.domino.base.dao.bean.TaskAwardConfigModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter@Setter
public class TaskAwardsModel extends StoredObj {
    private Map<String,TaskAwardConfigModel> taskAwards = new ConcurrentHashMap<>();
}
