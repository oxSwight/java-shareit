package ru.practicum.shareit.core.item.inside.entity.dto;

import ru.practicum.shareit.core.item.inside.entity.model.Comment;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;

public class CommentDtoMapper {
    private CommentDtoMapper() {

    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentDto commentDto, Item item, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }
}