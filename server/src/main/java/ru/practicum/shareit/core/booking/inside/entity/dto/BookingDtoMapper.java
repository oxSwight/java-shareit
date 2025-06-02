package ru.practicum.shareit.core.booking.inside.entity.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.core.booking.BookingStatus;
import ru.practicum.shareit.core.booking.inside.entity.model.Booking;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDtoMapper;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDtoMapper;

@UtilityClass
public class BookingDtoMapper {

    public static Booking toBooking(BookingInDto bookingDto, Item item, User user) {
        return Booking.builder()
                .end(bookingDto.getEnd())
                .start(bookingDto.getStart())
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(user)
                .build();
    }

    public static BookingOutDto toBookingDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserDtoMapper.toUserDto(booking.getBooker()))
                .item(ItemDtoMapper.toItemDto(booking.getItem()))
                .build();
    }
}