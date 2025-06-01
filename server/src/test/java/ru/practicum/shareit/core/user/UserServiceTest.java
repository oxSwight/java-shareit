package ru.practicum.shareit.core.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.DuplicatedDataException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findByIdShouldReturnUserWhenExists() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@mail.ru", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findByIdShouldThrowExceptionWhenUserNotExists() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findById(userId)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createShouldSaveNewUser() {
        UserDto userDto = UserDto.builder()
                .name("New User")
                .email("new@mail.ru")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("New User")
                .email("new@mail.ru")
                .build();

        when(userRepository.saveAndFlush(any())).thenReturn(savedUser);
        when(userRepository.findAllByEmail(any())).thenReturn(Optional.empty());

        UserDto result = userService.create(userDto);

        assertNotNull(result.getId());
        assertEquals("New User", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
        verify(userRepository, times(1)).saveAndFlush(any());
        verify(userRepository, times(1)).findAllByEmail("new@mail.ru");
    }

    @Test
    void createShouldThrowExceptionWhenEmailExists() {
        UserDto userDto = UserDto.builder()
                .name("New User")
                .email("existing@mail.ru")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .name("Existing User")
                .email("existing@mail.ru")
                .build();

        when(userRepository.findAllByEmail("existing@mail.ru")).thenReturn(Optional.of(existingUser));

        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> userService.create(userDto)
        );
        assertEquals("Этот email уже используется", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
        verify(userRepository, times(1)).findAllByEmail("existing@mail.ru");
    }

    @Test
    void updateShouldUpdateAllFields() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@mail.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .id(userId)
                .name("New Name")
                .email("new@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any())).thenReturn(existingUser);
        when(userRepository.findAllByEmail("new@mail.ru")).thenReturn(Optional.empty());

        UserDto result = userService.update(userId, updateDto);

        assertEquals(userId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).saveAndFlush(existingUser);
        verify(userRepository, times(1)).findAllByEmail("new@mail.ru");
    }

    @Test
    void updateShouldUpdateOnlyName() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@mail.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .id(userId)
                .name("New Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any())).thenReturn(existingUser);

        UserDto result = userService.update(userId, updateDto);

        assertEquals(userId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("old@mail.ru", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).saveAndFlush(existingUser);
    }

    @Test
    void updateShouldUpdateOnlyEmail() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@mail.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .id(userId)
                .email("new@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any())).thenReturn(existingUser);
        when(userRepository.findAllByEmail("new@mail.ru")).thenReturn(Optional.empty());

        UserDto result = userService.update(userId, updateDto);

        assertEquals(userId, result.getId());
        assertEquals("Old Name", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).saveAndFlush(existingUser);
        verify(userRepository, times(1)).findAllByEmail("new@mail.ru");
    }

    @Test
    void updateShouldThrowExceptionWhenUserNotExists() {
        Long userId = 999L;
        UserDto updateDto = UserDto.builder()
                .id(userId)
                .name("New Name")
                .email("new@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(userId, updateDto)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateShouldThrowExceptionWhenEmailExists() {
        Long userId = 1L;
        Long otherUserId = 2L;
        User existingUser = User.builder()
                .id(userId)
                .name("User 1")
                .email("user1@mail.ru")
                .build();

        User otherUser = User.builder()
                .id(otherUserId)
                .name("User 2")
                .email("existing@mail.ru")
                .build();

        UserDto updateDto = UserDto.builder()
                .id(userId)
                .name("User 1")
                .email("existing@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findAllByEmail("existing@mail.ru")).thenReturn(Optional.of(otherUser));

        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> userService.update(userId, updateDto)
        );
        assertEquals("Этот email уже используется", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findAllByEmail("existing@mail.ru");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteShouldDeleteExistingUser() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@mail.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.delete(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteShouldThrowExceptionWhenUserNotExists() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.delete(userId)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}