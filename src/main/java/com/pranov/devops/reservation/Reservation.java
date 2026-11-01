package com.pranov.devops.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "reserved_date", nullable = false)
    private LocalDate date;

    @Column(name = "reserved_time", nullable = false)
    private LocalTime time;

    @Column(length = 200)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Reservation() {
        // for JPA
    }

    public Reservation(String customerName, String phone, int partySize, LocalDate date, LocalTime time,
                       String notes, LocalDateTime createdAt) {
        this.customerName = customerName;
        this.phone = phone;
        this.partySize = partySize;
        this.date = date;
        this.time = time;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public int getPartySize() {
        return partySize;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getNotes() {
        return notes;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
