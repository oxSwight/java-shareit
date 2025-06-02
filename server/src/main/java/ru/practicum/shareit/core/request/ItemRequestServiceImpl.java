package ru.practicum.shareit.core.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDtoMapper;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.item.inside.repository.ItemRepository;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.core.request.inside.entity.model.ItemRequest;
import ru.practicum.shareit.core.request.inside.repository.ItemRequestRepository;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    private static final String NOT_FOUND_REQUEST = "Запрос не найден";
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    @Override
    public List<ItemRequestDto> findAllOwn(Long userId) {
        User user = getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user.getId());
        List<Item> items = itemRepository.findAllByRequestIdIn(requests.stream().map(ItemRequest::getId).toList());
        return requests.stream()
                .map(request -> ItemRequestDtoMapper.toItemRequestDto(request,
                        items.stream()
                                .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                                .map(ItemDtoMapper::toItemShortDto)
                                .toList()))
                .toList();
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        User user = getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(user.getId());
        List<Item> items = itemRepository.findAllByRequestIdIn(requests.stream().map(ItemRequest::getId).toList());
        return requests.stream()
                .map(request -> ItemRequestDtoMapper.toItemRequestDto(request,
                        items.stream()
                                .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                                .map(ItemDtoMapper::toItemShortDto)
                                .toList()))
                .toList();
    }

    @Override
    public ItemRequestDto findById(Long itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REQUEST));
        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(itemRequest.getId()));
        return ItemRequestDtoMapper.toItemRequestDto(itemRequest,
                items.stream().map(ItemDtoMapper::toItemShortDto).toList());
    }

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User user = getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestDtoMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestDtoMapper.toItemRequestDto(itemRequestRepository.saveAndFlush(itemRequest));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));
    }
}
