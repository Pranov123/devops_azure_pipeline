package com.pranov.devops.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.version=9.9.9", "app.build=123", "app.message=Test message"})
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageShowsAppDetails() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DevOps Azure Pipeline")))
                .andExpect(content().string(containsString("Test message")))
                .andExpect(content().string(containsString("9.9.9")));
    }

    @Test
    void infoEndpointReturnsJson() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("9.9.9"))
                .andExpect(jsonPath("$.build").value("123"));
    }

    @Test
    void addEndpointReturnsSum() throws Exception {
        mockMvc.perform(get("/api/add").param("a", "4").param("b", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(10));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
