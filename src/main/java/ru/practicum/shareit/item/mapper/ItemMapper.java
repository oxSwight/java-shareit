package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.comment.model.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.getIsAvailable())
                .build();
    }

    public static ItemDto toItemDto(Item item, List<CommentDto> comments) {
        ItemDto itemOwnerDto = ItemDto.builder()
                .id(item.getId())
                .isAvailable(item.getIsAvailable())
                .name(item.getName())
                .description(item.getDescription())
                .build();

        itemOwnerDto.setComments(comments);
        return itemOwnerDto;
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .owner(owner)
                .build();
    }
}