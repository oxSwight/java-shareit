package ru.practicum.shareit.core.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingOutDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto create(@RequestBody
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) BookingInDto bookingDto,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto updateStatus(@PathVariable Long bookingId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestParam Boolean approved) {
        return bookingService.updateStatus(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findById(@PathVariable Long bookingId,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingOutDto> findAllByBookerAndState(@RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findAllByBookerAndState(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllByOwnerAndState(@RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findAllByOwnerAndState(state, userId);
    }
}
