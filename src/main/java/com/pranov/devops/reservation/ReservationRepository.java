package com.pranov.devops.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateOrderByTimeAsc(LocalDate date);

    long countByDateAndTimeAndStatus(LocalDate date, LocalTime time, ReservationStatus status);
}
