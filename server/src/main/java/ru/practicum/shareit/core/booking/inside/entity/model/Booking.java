package ru.practicum.shareit.core.booking.inside.entity.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.core.booking.BookingStatus;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.user.inside.entity.model.User;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "item_id")
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @ManyToOne
    private Item item;

    @ManyToOne
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;
}