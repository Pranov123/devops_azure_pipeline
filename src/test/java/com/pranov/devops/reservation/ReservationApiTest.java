package com.pranov.devops.reservation;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationApiTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Kolkata"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions book(LocalDate date, String time, int partySize, String phone) throws Exception {
        String body = """
                {"customerName": "Priya", "phone": "%s", "partySize": %d, "date": "%s", "time": "%s",
                 "notes": "Window seat"}
                """.formatted(phone, partySize, date, time);
        return mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void booksTable() throws Exception {
        book(TODAY.plusDays(1), "19:00", 4, "9876543210")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.partySize").value(4))
                .andExpect(jsonPath("$.time").value("19:00:00"));
    }

    @Test
    void rejectsPastDate() throws Exception {
        book(TODAY.minusDays(1), "19:00", 2, "9876543210")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Reservation time must be in the future"));
    }

    @Test
    void rejectsTimeOutsideOpeningHours() throws Exception {
        book(TODAY.plusDays(1), "23:00", 2, "9876543210")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("We take reservations between 11:00 and 22:30"));
    }

    @Test
    void rejectsTimeNotOnHalfHourSlot() throws Exception {
        book(TODAY.plusDays(1), "19:15", 2, "9876543210")
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsTooFarAhead() throws Exception {
        book(TODAY.plusDays(61), "19:00", 2, "9876543210")
                .andExpect(status().isBadRequest());
    }

    @Test
    void validatesPartySizeAndPhone() throws Exception {
        book(TODAY.plusDays(1), "19:00", 0, "12345")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "partySize must be greater than or equal to 1, phone must be a 10-digit mobile number"));
    }

    @Test
    void rejectsBookingWhenSlotIsFull() throws Exception {
        LocalDate date = TODAY.plusDays(5);
        for (int i = 0; i < ReservationService.TABLES_PER_SLOT; i++) {
            book(date, "20:30", 2, "9876543210").andExpect(status().isCreated());
        }
        book(date, "20:30", 2, "9876543210")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Sorry, the 20:30 slot on " + date + " is fully booked"));
    }

    @Test
    void cancelsReservationAndListsByDate() throws Exception {
        LocalDate date = TODAY.plusDays(2);
        String json = book(date, "13:00", 3, "9123456789")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(patch("/api/reservations/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/reservations").param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
    }
}
