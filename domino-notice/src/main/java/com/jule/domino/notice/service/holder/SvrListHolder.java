package com.jule.domino.notice.service.holder;

import com.jule.domino.notice.model.GateServerInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储服务器列表
 */
public class SvrListHolder {
    public static Map<String, GateServerInfo> GATE_SVR_MAP = new HashMap<String, GateServerInfo>();

}
