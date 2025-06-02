package ru.practicum.shareit.core.request.inside.entity.dto;

import ru.practicum.shareit.core.item.inside.entity.dto.ItemShortDto;
import ru.practicum.shareit.core.request.inside.entity.model.ItemRequest;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestDtoMapper {
    private ItemRequestDtoMapper() {

    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemShortDto> items) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .requester(user)
                .build();
    }
}