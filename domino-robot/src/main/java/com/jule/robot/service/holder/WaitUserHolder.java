package com.jule.robot.service.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitUserHolder {
    public final static Map<String, AtomicInteger> SINGLE_USER_WAIT_MAP = new ConcurrentHashMap<>();
    public final static Map<String, AtomicInteger> TWO_USER_WAIT_MAP = new ConcurrentHashMap<>();
    public final static Map<String, String> GAMEID_CHECKING_MAP = new ConcurrentHashMap<>();
    public final static Map<String, AtomicInteger> THREE_USER_WAIT_MAP = new ConcurrentHashMap<>();
}
