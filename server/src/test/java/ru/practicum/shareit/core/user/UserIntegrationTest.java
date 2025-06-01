package ru.practicum.shareit.core.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShareItApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserIntegrationTest {
    static int userCount = 0;
    @Autowired
    private UserController userController;

    @Test
    void userControllerCreatesCorrectUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        assertNotNull(userDto.getId());
    }

    @Test
    void userControllerGetsUserById() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        UserDto createdUser = userController.findById(userDto.getId());
        assertEquals(userDto, createdUser);
    }

    @Test
    void userControllerUpdatesUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        userDto.setName("User" + 9999);
        userDto.setEmail("user" + 9999 + "@mail.ru");
        UserDto updatedUser = userController.update(userDto.getId(), userDto);
        assertEquals(userDto, updatedUser);
    }

    @Test
    void userControllerDeletesUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();
        userController.delete(userDto.getId());
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> userController.findById(userId),
                "Контроллер не удалил пользователя");
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    private UserDto getUserDto(int count) {
        userCount++;
        return UserDto.builder()
                .name("User" + count)
                .email("user" + count + "@mail.ru")
                .build();
    }
}