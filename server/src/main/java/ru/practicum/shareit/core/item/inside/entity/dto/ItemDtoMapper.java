package ru.practicum.shareit.core.item.inside.entity.dto;

import ru.practicum.shareit.core.booking.inside.entity.dto.BookingDtoMapper;
import ru.practicum.shareit.core.booking.inside.entity.model.Booking;
import ru.practicum.shareit.core.item.inside.entity.model.Comment;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ItemDtoMapper {
    private ItemDtoMapper() {

    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemShortDto toItemShortDto(Item item) {
        return ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }

    public static ItemDto toItemDto(Item item, List<Booking> bookings, List<Comment> comments) {
        ItemDto itemOwnerDto = ItemDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .name(item.getName())
                .description(item.getDescription())
                .build();

        Optional<Booking> currentBooking = bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                .findFirst();

        currentBooking.ifPresent(booking -> itemOwnerDto.setLastBooking(BookingDtoMapper.toBookingDto(booking)));

        Optional<Booking> nextBooking = bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart));

        nextBooking.ifPresent(booking -> itemOwnerDto.setNextBooking(BookingDtoMapper.toBookingDto(booking)));

        List<CommentDto> commentList = comments.stream()
                .filter(comment -> comment.getItem().getId().equals(item.getId()))
                .map(CommentDtoMapper::toCommentDto)
                .toList();

        itemOwnerDto.setComments(commentList);
        return itemOwnerDto;
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();
    }

}