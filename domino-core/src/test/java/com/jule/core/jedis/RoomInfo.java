package com.jule.core.jedis;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper=false)
public class RoomInfo extends StoredObj {
    private String roomId;
    private Map<String,TableInfo> map = new ConcurrentHashMap<>();

}
