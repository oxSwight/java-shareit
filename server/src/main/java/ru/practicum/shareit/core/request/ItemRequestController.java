package ru.practicum.shareit.core.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestBody ItemRequestDto itemRequest,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(itemRequest, userId);
    }

    @GetMapping
    public List<ItemRequestDto> findAllOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAllOwn(userId);
    }

    @GetMapping("/{id}")
    public ItemRequestDto findById(@PathVariable Long id,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findById(id);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAll(userId);
    }
}