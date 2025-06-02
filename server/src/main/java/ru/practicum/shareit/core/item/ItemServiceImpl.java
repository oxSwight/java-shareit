package ru.practicum.shareit.core.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.core.booking.inside.entity.model.Booking;
import ru.practicum.shareit.core.booking.inside.repository.BookingRepository;
import ru.practicum.shareit.core.item.inside.entity.dto.CommentDto;
import ru.practicum.shareit.core.item.inside.entity.dto.CommentDtoMapper;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDtoMapper;
import ru.practicum.shareit.core.item.inside.entity.model.Comment;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.item.inside.repository.CommentRepository;
import ru.practicum.shareit.core.item.inside.repository.ItemRepository;
import ru.practicum.shareit.core.request.inside.entity.model.ItemRequest;
import ru.practicum.shareit.core.request.inside.repository.ItemRequestRepository;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private static final String NOT_FOUND_ITEM = "Предмет не найден";
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    @Override
    public List<ItemDto> findAllOwned(Long ownerId) {
        User owner = getUserOrThrow(ownerId);
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByStartAsc(owner.getId());
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());
        List<Comment> comments = commentRepository.findAllByItemIdIn(items.stream().map(Item::getId).toList());
        return items.stream()
                .map(item -> ItemDtoMapper.toItemDto(item, bookings, comments))
                .toList();
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM));
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStartAsc(itemId);
        ItemDto itemDto = ItemDtoMapper.toItemDto(item, bookings, comments);
        itemDto.setComments(comments.stream().map(CommentDtoMapper::toCommentDto).toList());
        return itemDto;
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = getUserOrThrow(userId);
        Item item = ItemDtoMapper.toItem(itemDto, user);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }

        return ItemDtoMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        User user = getUserOrThrow(userId);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM));

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new ConditionsNotMetException("Пользователь не владелец предмета");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemDtoMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.findAllBySearch(text).stream()
                .map(ItemDtoMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto createComment(Long itemId, CommentDto commentDto, Long userId) {
        User user = getUserOrThrow(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM));

        bookingRepository.findByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now())
                .orElseThrow(() -> new ConditionsNotMetException("Пользователь не арендовал предмет или время аренды еще не вышло"));

        Comment comment = CommentDtoMapper.toComment(commentDto, item, user);
        return CommentDtoMapper.toCommentDto(commentRepository.saveAndFlush(comment));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));
    }
}