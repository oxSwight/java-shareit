package ru.practicum.shareit.core.request.inside.entity.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    private User requester;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

}