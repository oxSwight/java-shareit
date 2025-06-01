package ru.practicum.shareit.core.user.inside.entity.dto;

import ru.practicum.shareit.core.user.inside.entity.model.User;

public class UserDtoMapper {
    private UserDtoMapper() {

    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

}