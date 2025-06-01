package ru.practicum.shareit.core.item;

import ru.practicum.shareit.core.item.inside.entity.dto.CommentDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;

import java.util.List;


public interface ItemService {
    List<ItemDto> findAllOwned(Long ownerId);

    ItemDto findById(Long itemId);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long id, ItemDto itemDto, Long userId);

    List<ItemDto> search(String text);

    CommentDto createComment(Long itemId, CommentDto commentDto, Long userId);
}