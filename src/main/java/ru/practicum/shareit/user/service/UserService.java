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
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto getItemDto(Long id) {
        return UserMapper.toUserDto(getUser(id));
    }

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateUser(user);
        return UserMapper.toUserDto(userRepository.saveAndFlush(user));
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = getUser(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        validateUser(user);
        return UserMapper.toUserDto(userRepository.saveAndFlush(user));
    }

    public void deleteUser(Long id) {
        getUser(id);
        userRepository.deleteById(id);
    }

    private void validateUser(User user) {
        if (userRepository.findAllByEmail(user.getEmail())
                .stream()
                .anyMatch(u ->
                        !Objects.equals(u.getId(), user.getId()))) {
            throw new DuplicateException("Этот email уже используется");
        }
    }

    public User getUser(Long userId) throws ConditionsNotMetException {
        if (userId == null) {
            throw new ConditionsNotMetException(ExceptionMessages.NOT_FOUND_ITEM);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.USER_NOT_FOUND_ERROR, userId)));
    }

}