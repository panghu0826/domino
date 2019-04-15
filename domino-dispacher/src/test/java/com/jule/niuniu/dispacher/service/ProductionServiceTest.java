package com.jule.domino.dispacher.service;

import com.jule.domino.base.service.ItemServer;
import com.jule.domino.dispacher.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductionServiceTest {

    @BeforeEach
    void setUp() {
        Config.load();
        ItemServer.OBJ.init(Config.ITEM_SERVER_URL,Config.GAME_ID);
        ProductionService.getInstance().discoverData();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getInstance() {
    }

    @Test
    void getGiftConfig() {
    }

    @Test
    void discoverData() {
    }

    @Test
    void getAllGiftConfig() {
        ProductionService.getInstance().getAllGiftConfig();
    }

    @Test
    void getLimitGiftConfig() {
    }
}