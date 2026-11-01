package com.pranov.devops.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record ReservationRequest(
            @NotBlank @Size(max = 80) String customerName,
            @NotBlank @Pattern(regexp = "[6-9][0-9]{9}", message = "must be a 10-digit mobile number") String phone,
            @Min(1) @Max(12) int partySize,
            @NotNull LocalDate date,
            @NotNull LocalTime time,
            @Size(max = 200) String notes) {
    }

    public record ReservationResponse(Long id, String customerName, String phone, int partySize,
                                      LocalDate date, LocalTime time, String notes, ReservationStatus status) {

        static ReservationResponse from(Reservation r) {
            return new ReservationResponse(r.getId(), r.getCustomerName(), r.getPhone(), r.getPartySize(),
                    r.getDate(), r.getTime(), r.getNotes(), r.getStatus());
        }
    }
}
