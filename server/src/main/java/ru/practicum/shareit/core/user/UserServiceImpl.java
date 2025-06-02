package ru.practicum.shareit.core.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DuplicatedDataException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDtoMapper;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    public UserDto findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return UserDtoMapper.toUserDto(user.orElseThrow(()  -> new NotFoundException(NOT_FOUND_USER)));
    }

    public UserDto create(UserDto userDto) {
        User user = UserDtoMapper.toUser(userDto);
        validate(user);
        return UserDtoMapper.toUserDto(userRepository.saveAndFlush(user));
    }

    public UserDto update(Long id, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        User user = userOptional.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        validate(user);
        userRepository.saveAndFlush(user);
        return UserDtoMapper.toUserDto(user);
    }

    public void delete(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));
        userRepository.deleteById(id);
    }

    private void validate(User user) throws DuplicatedDataException {
        Optional<User> userOptional = userRepository.findAllByEmail(user.getEmail());
        if (userOptional.isPresent() && !userOptional.get().getId().equals(user.getId())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
    }
}