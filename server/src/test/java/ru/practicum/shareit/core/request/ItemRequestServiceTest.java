package ru.practicum.shareit.core.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.item.inside.repository.ItemRepository;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;
import ru.practicum.shareit.core.request.inside.entity.model.ItemRequest;
import ru.practicum.shareit.core.request.inside.repository.ItemRequestRepository;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User createUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    private ItemRequest createItemRequest(Long id, User requester) {
        return ItemRequest.builder()
                .id(id)
                .description("Need item for testing")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    private Item createItem(Long id, String name, User owner, ItemRequest request) {
        return Item.builder()
                .id(id)
                .name(name)
                .description("Test item")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }

    @Test
    void findAllOwnShouldReturnEmptyListWhenNoRequests() {
        Long userId = 1L;
        User requester = createUser(userId, "Requester", "requester@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(Collections.emptyList());
        when(itemRepository.findAllByRequestIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.findAllOwn(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }


    @Test
    void findAllOwnShouldReturnRequestsWithItems() {
        Long userId = 1L;
        User requester = createUser(userId, "Requester", "requester@email.com");
        ItemRequest request1 = createItemRequest(1L, requester);
        ItemRequest request2 = createItemRequest(2L, requester);

        User owner = createUser(3L, "Owner", "owner@email.com");
        Item item1 = createItem(1L, "Item 1", owner, request1);
        Item item2 = createItem(2L, "Item 2", owner, request1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(request1, request2));
        when(itemRepository.findAllByRequestIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(item1, item2));

        List<ItemRequestDto> result = itemRequestService.findAllOwn(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2, result.get(0).getItems().size());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(1L, 2L));
    }


    @Test
    void findAllShouldReturnEmptyListWhenNoOtherRequests() {
        Long userId = 1L;
        User user = createUser(userId, "User", "user@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId))
                .thenReturn(Collections.emptyList());
        when(itemRepository.findAllByRequestIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.findAll(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterIdNotOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }


    @Test
    void findAllShouldReturnOtherUsersRequests() {
        Long userId = 1L;
        User currentUser = createUser(userId, "Current", "current@email.com");
        User otherUser = createUser(2L, "Other", "other@email.com");

        ItemRequest request1 = createItemRequest(1L, otherUser);
        ItemRequest request2 = createItemRequest(2L, otherUser);

        User owner = createUser(3L, "Owner", "owner@email.com");
        Item item1 = createItem(1L, "Item 1", owner, request1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId))
                .thenReturn(List.of(request1, request2));
        when(itemRepository.findAllByRequestIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(item1));

        List<ItemRequestDto> result = itemRequestService.findAll(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(0, result.get(1).getItems().size());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterIdNotOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(1L, 2L));
    }


    @Test
    void findByIdShouldThrowWhenRequestNotFound() {
        Long requestId = 1L;
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.findById(requestId));
        verify(itemRequestRepository).findById(requestId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void findByIdShouldReturnRequestWithItems() {
        Long requestId = 1L;
        User requester = createUser(1L, "Requester", "requester@email.com");
        ItemRequest request = createItemRequest(requestId, requester);
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item1 = createItem(1L, "Item 1", owner, request);
        Item item2 = createItem(2L, "Item 2", owner, request);

        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestIdIn(List.of(requestId)))
                .thenReturn(List.of(item1, item2));

        ItemRequestDto result = itemRequestService.findById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(2, result.getItems().size());
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).findAllByRequestIdIn(List.of(requestId));
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.create(requestDto, userId));
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void createShouldSaveNewRequest() {
        Long userId = 1L;
        User requester = createUser(userId, "Requester", "requester@email.com");
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need item")
                .build();
        ItemRequest savedRequest = createItemRequest(1L, requester);
        savedRequest.setDescription(requestDto.getDescription());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requester));
        when(itemRequestRepository.saveAndFlush(any(ItemRequest.class)))
                .thenReturn(savedRequest);

        ItemRequestDto result = itemRequestService.create(requestDto, userId);

        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.getId());
        assertEquals(requestDto.getDescription(), result.getDescription());
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).saveAndFlush(any(ItemRequest.class));
    }
}