package com.rasto.accommodationbookingsystem.backend.service;

import com.rasto.accommodationbookingsystem.backend.data.entity.Booking;
import com.rasto.accommodationbookingsystem.exception.UserNotAuthenticatedException;

import java.time.LocalDate;
import java.util.List;

public interface BookingsService extends CrudService<Booking> {

    List<LocalDate> getBookedDays(Long accommodationId);

    /**
     * @param checkIn date from (inclusive)
     * @param checkOut date to (exclusive)
     * @param accommodationId accommodation id
     * @return true if some day exists between checkIn and checkOut range in accommodation (accommodationId) and is booked
     */
    boolean existsBookedDayBetween(LocalDate checkIn, LocalDate checkOut, Long accommodationId);

    /**
     * Make booking
     * @param booking
     * @param accommodationId
     * @param userId
     */
    void bookAccommodation(Booking booking, Long accommodationId, Long userId) throws UserNotAuthenticatedException;
}
