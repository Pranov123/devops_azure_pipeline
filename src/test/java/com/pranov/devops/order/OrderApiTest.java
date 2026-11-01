package com.pranov.devops.order;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Seeded menu ids: 1 = Paneer Tikka (249.00), 12 = Butter Naan (59.00), 19 = Mango Lassi. */
@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long placeOrder(String body) throws Exception {
        String json = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void placesOrderAndCalculatesBillWithGst() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName": "Asha", "orderType": "DINE_IN", "tableNumber": 4,
                         "items": [{"menuItemId": 1, "quantity": 2}, {"menuItemId": 12, "quantity": 3}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.subtotal").value(675.00))
                .andExpect(jsonPath("$.tax").value(33.75))
                .andExpect(jsonPath("$.total").value(708.75));
    }

    @Test
    void mergesRepeatedDishesIntoOneLine() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName": "Ravi", "orderType": "TAKEAWAY",
                         "items": [{"menuItemId": 12, "quantity": 1}, {"menuItemId": 12, "quantity": 2}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.tableNumber").doesNotExist());
    }

    @Test
    void dineInNeedsTableNumber() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName": "Asha", "orderType": "DINE_IN", "items": [{"menuItemId": 1, "quantity": 1}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Table number is required for dine-in orders"));
    }

    @Test
    void rejectsEmptyOrder() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName": "Asha", "orderType": "TAKEAWAY", "items": []}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("items must not be empty"));
    }

    @Test
    void rejectsUnavailableDish() throws Exception {
        mockMvc.perform(patch("/api/menu/19/availability").contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\": false}")).andExpect(status().isOk());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName": "Asha", "orderType": "TAKEAWAY", "items": [{"menuItemId": 19, "quantity": 1}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Mango Lassi is not available right now"));

        mockMvc.perform(patch("/api/menu/19/availability").contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\": true}")).andExpect(status().isOk());
    }

    @Test
    void movesThroughKitchenWorkflow() throws Exception {
        long id = placeOrder("""
                {"customerName": "Meena", "orderType": "TAKEAWAY", "items": [{"menuItemId": 1, "quantity": 1}]}
                """);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        for (String next : new String[] {"PREPARING", "READY", "SERVED"}) {
            mockMvc.perform(patch("/api/orders/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"" + next + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(jsonPath("$.status").value("SERVED"))
                .andExpect(jsonPath("$.nextStatuses.length()").value(0));
    }

    @Test
    void rejectsSkippingWorkflowSteps() throws Exception {
        long id = placeOrder("""
                {"customerName": "Kiran", "orderType": "TAKEAWAY", "items": [{"menuItemId": 12, "quantity": 1}]}
                """);

        mockMvc.perform(patch("/api/orders/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SERVED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot change order " + id + " from PLACED to SERVED"));
    }

    @Test
    void unknownOrderIsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound());
    }
}
