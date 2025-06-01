package ru.practicum.shareit.core.item.inside.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.item.inside.entity.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("select item from Item item " +
            "where item.available = TRUE and (upper(item.name) like upper(concat('%', ?1, '%')) " +
            "or upper(item.description) like upper(concat('%', ?1, '%')))")
    List<Item> findAllBySearch(String search);

    List<Item> findAllByRequestIdIn(List<Long> requestIdList);
}