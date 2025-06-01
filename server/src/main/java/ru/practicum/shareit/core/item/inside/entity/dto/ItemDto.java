package ru.practicum.shareit.core.item.inside.entity.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingOutDto;

import java.util.List;

@Data
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingOutDto lastBooking;
    private BookingOutDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;

}