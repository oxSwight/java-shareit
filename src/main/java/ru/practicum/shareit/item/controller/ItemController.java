package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody @Valid ItemDto item,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на создание предмета {} пользователем {}", item, userId);
        ItemDto itemDto = itemService.create(item, userId);
        log.info("Предмет {} успешно создан", itemDto);
        return itemDto;
    }
    @GetMapping
    public List<ItemDto> findAllOwned(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение предметов пользователя {}", userId);
        List<ItemDto> itemDtos = itemService.getUserItems(userId);
        log.info("Предметы {} успешно получены", itemDtos);
        return itemDtos;
    }

    @GetMapping("/{id}")
    public ItemDto findItemById(@PathVariable Long id,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на получение предмета {} пользователем {}", id, userId);
        ItemDto itemDto = itemService.read(id);
        log.info("Предмет {} успешно получен", itemDto);
        return itemDto;
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос поиска предметов {} пользователем {}", text, userId);
        List<ItemDto> itemDtos = itemService.search(text);
        log.info("Предметы {} успешно найдены", itemDtos);
        return itemDtos;
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable Long id,
                          @RequestBody ItemDto item,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на обновление предмета {} пользователем {}", item, userId);
        ItemDto itemDto = itemService.update(id, item, userId);
        log.info("Предмет {} успешно обновлен", itemDto);
        return itemDto;
    }
}

