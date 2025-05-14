package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.DuplicateException;
import ru.practicum.shareit.exceptions.ExceptionMessages;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public UserDto read(Long id) {
        return UserMapper.toUserDto(userStorage.read(id));
    }

    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validate(user);
        return UserMapper.toUserDto(userStorage.create(user));
    }

    public UserDto update(Long id, UserDto userDto) {
        User user = userStorage.read(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        validate(user);
        return UserMapper.toUserDto(userStorage.update(user));
    }

    public void delete(Long id) {
        exists(id);
        userStorage.delete(id);
    }

    private void validate(User user) throws DuplicateException {
        if (userStorage.getAll()
                .stream()
                .anyMatch(u -> u.getEmail()
                        .equals(user.getEmail()) && !Objects.equals(u.getId(), user.getId()))) {
            throw new DuplicateException("Этот email уже используется");
        }
    }

    public void exists(Long userId) throws ConditionsNotMetException {
        Optional.ofNullable(userStorage.read(userId))
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.USER_NOT_FOUND_ERROR, userId)));
    }

}
