package com.pranov.devops.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class InfoServiceTest {

    private final InfoService service = new InfoService("Test App", "2.0.0", "42", "Hello");

    @Test
    void infoContainsConfiguredValues() {
        Map<String, String> info = service.getInfo();
        assertEquals("Test App", info.get("application"));
        assertEquals("2.0.0", info.get("version"));
        assertEquals("42", info.get("build"));
        assertEquals("Hello", info.get("message"));
    }

    @Test
    void infoContainsRuntimeValues() {
        Map<String, String> info = service.getInfo();
        assertTrue(info.containsKey("hostname"));
        assertFalse(info.get("hostname").isBlank());
        assertTrue(info.containsKey("startedAt"));
    }

    @Test
    void addReturnsSum() {
        assertEquals(5, InfoService.add(2, 3));
        assertEquals(-1, InfoService.add(2, -3));
        assertEquals(0, InfoService.add(0, 0));
    }
}
