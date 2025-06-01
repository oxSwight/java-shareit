package ru.practicum.shareit.core.item.inside.entity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;
}