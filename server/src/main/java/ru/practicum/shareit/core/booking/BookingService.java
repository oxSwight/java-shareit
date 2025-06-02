package ru.practicum.shareit.core.booking;

import ru.practicum.shareit.core.booking.inside.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto create(BookingInDto booking, Long userId);

    BookingOutDto updateStatus(Long bookingId, Long userId, Boolean approved);

    BookingOutDto findById(Long bookingId, Long userId);

    List<BookingOutDto> findAllByBookerAndState(BookingState state, Long userId);

    List<BookingOutDto> findAllByOwnerAndState(BookingState state, Long userId);
}