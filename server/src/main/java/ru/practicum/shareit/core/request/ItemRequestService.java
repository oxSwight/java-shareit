package ru.practicum.shareit.core.request;

import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestDto> findAllOwn(Long userId);

    List<ItemRequestDto> findAll(Long userId);

    ItemRequestDto findById(Long itemRequestId);

    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);
}