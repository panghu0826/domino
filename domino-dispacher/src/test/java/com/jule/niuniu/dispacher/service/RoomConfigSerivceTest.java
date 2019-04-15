package com.jule.domino.dispacher.service;

import com.jule.domino.dispacher.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomConfigSerivceTest {

    @BeforeEach
    void setUp() {
        Config.load();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void init() {
        RoomConfigSerivce.OBJ.init();
    }

    @Test
    void loadData() {
    }

    @Test
    void getRoomConfigs() {
    }

    @Test
    void main() {
    }
}