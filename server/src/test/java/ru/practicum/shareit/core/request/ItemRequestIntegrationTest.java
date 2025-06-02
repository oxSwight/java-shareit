package ru.practicum.shareit.core.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.core.item.ItemService;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.core.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemRequestIntegrationTest {
    static int userCount = 0;
    static int itemCount = 0;

    @Autowired
    private ItemRequestController itemRequestController;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createShouldCreateNewRequest() {
        UserDto userDto = userService.create(getUserDto(userCount));

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need item for testing")
                .build();

        requestDto = itemRequestController.create(requestDto, userDto.getId());

        assertNotNull(requestDto.getId());
        assertEquals("Need item for testing", requestDto.getDescription());
    }

    @Test
    void findAllOwnShouldReturnUserRequests() {
        UserDto userDto = userService.create(getUserDto(userCount));

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need item for testing")
                .build();

        requestDto = itemRequestController.create(requestDto, userDto.getId());

        List<ItemRequestDto> result = itemRequestController.findAllOwn(userDto.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestDto.getId(), result.get(0).getId());
    }

    @Test
    void findByIdShouldReturnRequestWithItems() {
        UserDto requesterUserDto = userService.create(getUserDto(userCount));

        ItemRequestDto request = itemRequestController.create(ItemRequestDto.builder()
                .description("Test request")
                .build(), requesterUserDto.getId());

        UserDto ownerUserDto = userService.create(getUserDto(userCount));
        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setRequestId(request.getId());

        itemService.create(itemDto, ownerUserDto.getId());

        ItemRequestDto result = itemRequestController.findById(request.getId(), requesterUserDto.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void findAllShouldReturnOtherUsersRequests() {
        UserDto userDto1 = userService.create(getUserDto(userCount));

        UserDto userDto2 = userService.create(getUserDto(userCount));

        ItemRequestDto request = itemRequestController.create(ItemRequestDto.builder()
                .description("Test request")
                .build(), userDto1.getId());

        UserDto ownerUserDto = userService.create(getUserDto(userCount));
        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setRequestId(request.getId());

        itemService.create(itemDto, ownerUserDto.getId());

        List<ItemRequestDto> result = itemRequestController.findAll(userDto2.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findByIdShouldThrowWhenRequestNotFound() {
        UserDto userDto = userService.create(getUserDto(userCount));
        long userId = userDto.getId();
        long nonExistentId = 999L;

        assertThrows(Exception.class, () ->
                itemRequestController.findById(nonExistentId, userId));
    }

    private UserDto getUserDto(int count) {
        userCount++;
        return UserDto.builder()
                .name("User" + count)
                .email("user" + count + "@mail.ru")
                .build();
    }

    private ItemDto getItemDto(int count) {
        itemCount++;
        return ItemDto.builder()
                .name("Item" + count)
                .description("Description" + count)
                .available(true)
                .build();
    }
}