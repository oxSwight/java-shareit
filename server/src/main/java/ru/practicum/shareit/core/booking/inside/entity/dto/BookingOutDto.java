package ru.practicum.shareit.core.booking.inside.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.booking.BookingStatus;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingOutDto {
    private Long id;
    private ItemDto item;
    private UserDto booker;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;
    private BookingStatus status;
}