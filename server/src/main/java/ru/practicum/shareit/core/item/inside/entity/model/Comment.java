package ru.practicum.shareit.core.item.inside.entity.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    private Item item;

    @ManyToOne
    private User author;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}