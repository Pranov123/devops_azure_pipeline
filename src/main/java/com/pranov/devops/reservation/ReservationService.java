package com.pranov.devops.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pranov.devops.common.BusinessRuleException;
import com.pranov.devops.common.NotFoundException;
import com.pranov.devops.reservation.ReservationDtos.ReservationRequest;
import com.pranov.devops.reservation.ReservationDtos.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    static final LocalTime FIRST_SLOT = LocalTime.of(11, 0);
    static final LocalTime LAST_SLOT = LocalTime.of(22, 30);
    static final int TABLES_PER_SLOT = 8;
    static final int MAX_DAYS_AHEAD = 60;

    private final ReservationRepository repository;
    private final Clock clock;

    public ReservationService(ReservationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse book(ReservationRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate date = request.date();
        LocalTime time = request.time();

        if (time.isBefore(FIRST_SLOT) || time.isAfter(LAST_SLOT)) {
            throw new BusinessRuleException("We take reservations between 11:00 and 22:30");
        }
        if (time.getMinute() % 30 != 0 || time.getSecond() != 0) {
            throw new BusinessRuleException("Reservations are in 30-minute slots (e.g. 19:00 or 19:30)");
        }
        if (!LocalDateTime.of(date, time).isAfter(now)) {
            throw new BusinessRuleException("Reservation time must be in the future");
        }
        if (date.isAfter(now.toLocalDate().plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessRuleException("Reservations can be made up to " + MAX_DAYS_AHEAD + " days ahead");
        }
        if (repository.countByDateAndTimeAndStatus(date, time, ReservationStatus.CONFIRMED) >= TABLES_PER_SLOT) {
            throw new BusinessRuleException("Sorry, the " + time + " slot on " + date + " is fully booked");
        }

        String notes = request.notes() == null || request.notes().isBlank() ? null : request.notes().trim();
        Reservation reservation = new Reservation(request.customerName().trim(), request.phone(),
                request.partySize(), date, time, notes, now);
        return ReservationResponse.from(repository.save(reservation));
    }

    public ReservationResponse get(Long id) {
        return ReservationResponse.from(find(id));
    }

    public List<ReservationResponse> forDate(LocalDate date) {
        return repository.findByDateOrderByTimeAsc(date).stream().map(ReservationResponse::from).toList();
    }

    public List<ReservationResponse> today() {
        return forDate(LocalDate.now(clock));
    }

    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation reservation = find(id);
        reservation.cancel();
        return ReservationResponse.from(reservation);
    }

    private Reservation find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Reservation " + id + " not found"));
    }
}
