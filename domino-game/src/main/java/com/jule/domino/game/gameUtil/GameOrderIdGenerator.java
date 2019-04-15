package com.jule.domino.game.gameUtil;


import com.jule.domino.game.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 生成唯一游戏局号
 */
@Slf4j
public class GameOrderIdGenerator {

    public static String generate() {
        try {
            return String.valueOf(IdWorker.getInstance().nextId());
        }catch (Exception e){
            log.error("IdWorker生产id失败,启用备用方案 , exception = {}" ,e.getMessage());
            return UUID.randomUUID().toString();
        }
    }
}
