package ru.practicum.shareit.core.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.core.booking.BookingController;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingInDto;
import ru.practicum.shareit.core.item.inside.entity.dto.CommentDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.UserController;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemIntegrationTest {
    static int userCount = 0;
    static int itemCount = 0;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Autowired
    private BookingController bookingController;

    @Test
    void itemControllerCreatesCorrectItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        assertNotNull(itemDto.getId());
    }

    @Test
    void itemControllerFindsItemById() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        ItemDto foundItemDto = itemController.findById(itemDto.getId(), userDto.getId());
        assertEquals(itemDto.getId(), foundItemDto.getId());
    }

    @Test
    void itemControllerFindsAllItemsForUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemController.create(itemDto, userDto.getId());
        ItemDto itemDto2 = getItemDto(itemCount);
        itemController.create(itemDto2, userDto.getId());
        ItemDto itemDto3 = getItemDto(itemCount);
        itemController.create(itemDto3, userDto.getId());

        assertEquals(3, itemController.findAllOwned(userDto.getId()).size());
    }

    @Test
    void itemControllerUpdatesItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        itemDto.setName("Item9999");
        itemDto.setDescription("Desc9999");
        itemDto.setAvailable(false);
        ItemDto updatedItemDto = itemController.update(itemDto.getId(), itemDto, userDto.getId());
        assertEquals(itemDto, updatedItemDto);
    }

    @Test
    void itemControllerSearchesItemsByText() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setName("SearchItem");
        itemController.create(itemDto, userDto.getId());

        ItemDto itemDto2 = getItemDto(itemCount);
        itemDto2.setDescription("SearchItemDescription");
        itemController.create(itemDto2, userDto.getId());

        ItemDto itemDto3 = getItemDto(itemCount);
        itemDto3.setDescription("SearchItem");
        itemDto3.setDescription("SearchItemDescription");
        itemDto3.setAvailable(false);
        itemController.create(itemDto3, userDto.getId());

        ItemDto itemDto4 = getItemDto(itemCount);
        itemController.create(itemDto4, userDto.getId());

        assertEquals(2, itemController.search("SearchItem", userDto.getId()).size(), "Неверное количество найденных вещей");

    }

    @Test
    void itemControllerCreatesCommentForItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        bookingController.create(bookingDto, userDto.getId());

        CommentDto commentDto = getCommentDto("Comment");

        CommentDto resultCommentDto = itemController.createComment(itemDto.getId(), commentDto, userDto.getId());
        assertEquals(commentDto.getText(), resultCommentDto.getText());
        assertEquals(userDto.getName(), resultCommentDto.getAuthorName());
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

    private BookingInDto getBookingDto(ItemDto itemDto, LocalDateTime start, LocalDateTime end) {
        return BookingInDto.builder()
                .itemId(itemDto.getId())
                .start(start)
                .end(end)
                .build();
    }

    private CommentDto getCommentDto(String text) {
        return CommentDto.builder()
                .text(text)
                .build();
    }
}