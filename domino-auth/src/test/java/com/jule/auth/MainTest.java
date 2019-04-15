package com.jule.auth;

import com.jule.domino.auth.service.DispacherService;
import org.junit.jupiter.api.Test;

public class MainTest {
    @Test
    public void MainTest() {
        DispacherService.getInstance();
    }
}
