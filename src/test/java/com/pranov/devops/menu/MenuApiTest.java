package com.pranov.devops.menu;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

@SpringBootTest
@AutoConfigureMockMvc
class MenuApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededMenu() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(19))))
                .andExpect(jsonPath("$[*].name", hasItem("Chicken Dum Biryani")));
    }

    @Test
    void filtersByCategory() throws Exception {
        mockMvc.perform(get("/api/menu").param("category", "DESSERTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", everyItem(is("DESSERTS"))))
                .andExpect(jsonPath("$[*].name", hasItem("Gulab Jamun")));
    }

    @Test
    void listsCategoriesWithLabels() throws Exception {
        mockMvc.perform(get("/api/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[1].label").value("Main Course"));
    }

    @Test
    void addsNewDish() throws Exception {
        mockMvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Masala Dosa", "description": "Crisp rice crepe with potato masala",
                         "category": "MAINS", "price": 149.00, "vegetarian": true, "spicy": false}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.categoryLabel").value("Main Course"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void rejectsInvalidDish() throws Exception {
        mockMvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "", "description": "x", "category": "MAINS", "price": 0}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("name must not be blank, price must be greater than or equal to 1.00"));
    }

    @Test
    void togglesAvailability() throws Exception {
        mockMvc.perform(patch("/api/menu/18/availability").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(patch("/api/menu/18/availability").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void unknownDishIsNotFound() throws Exception {
        mockMvc.perform(get("/api/menu/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Menu item 9999 not found"));
    }
}
