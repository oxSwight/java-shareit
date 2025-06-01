package ru.practicum.shareit.core.request.inside.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.request.inside.entity.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long userId);

    List<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(Long userId);
}