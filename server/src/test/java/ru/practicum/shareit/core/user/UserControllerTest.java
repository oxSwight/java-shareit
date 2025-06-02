package ru.practicum.shareit.core.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.exceptions.DuplicatedDataException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.handlers.ErrorHandler;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static int userCount = 0;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createUserShouldCreateCorrectUser() throws Exception {
        UserDto userDto = getUserDto(userCount);
        when(userService.create(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).create(any());
    }

    @Test
    void createUserWithDuplicateEmailShouldThrowException() throws Exception {
        UserDto userDto = getUserDto(userCount);
        when(userService.create(any())).thenThrow(new DuplicatedDataException("Этот email уже используется"));

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Этот email уже используется"));

        verify(userService, times(1)).create(any());
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        UserDto userDto = getUserDto(userCount);
        when(userService.findById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", userDto.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).findById(userDto.getId());
    }

    @Test
    void getUserByIdWithWrongIdShouldThrowException() throws Exception {
        when(userService.findById(anyLong())).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));

        verify(userService, times(1)).findById(9999L);
    }

    @Test
    void updateUserShouldUpdateAllFields() throws Exception {
        UserDto originalUser = getUserDto(userCount);
        UserDto updatedUser = getUserDto(userCount);
        updatedUser.setName("UpdatedName");
        updatedUser.setEmail("updated@mail.ru");

        when(userService.update(anyLong(), any())).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", originalUser.getId())
                        .content(mapper.writeValueAsString(updatedUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()));

        verify(userService, times(1)).update(eq(originalUser.getId()), any());
    }

    @Test
    void updateUserWithPartialFieldsShouldUpdateOnlyProvidedFields() throws Exception {
        UserDto originalUser = getUserDto(userCount);
        originalUser.setId(1L);
        UserDto updateRequest = UserDto.builder().name("UpdatedNameOnly").build();

        UserDto expectedResponse = UserDto.builder()
                .id(originalUser.getId())
                .name(updateRequest.getName())
                .email(originalUser.getEmail())
                .build();

        when(userService.update(anyLong(), any())).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/{id}", originalUser.getId())
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.email").value(originalUser.getEmail()));

        verify(userService, times(1)).update(eq(originalUser.getId()), any());
    }

    @Test
    void updateUserWithDuplicateEmailShouldThrowException() throws Exception {
        UserDto userDto1 = getUserDto(userCount);
        UserDto userDto2 = getUserDto(userCount);
        userDto2.setEmail(userDto1.getEmail());

        when(userService.update(anyLong(), any())).thenThrow(new DuplicatedDataException("Этот email уже используется"));

        mockMvc.perform(patch("/users/{id}", userDto2.getId())
                        .content(mapper.writeValueAsString(userDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Этот email уже используется"));

        verify(userService, times(1)).update(eq(userDto2.getId()), any());
    }

    @Test
    void updateUserWithWrongIdShouldThrowException() throws Exception {
        UserDto userDto = getUserDto(userCount);
        when(userService.update(anyLong(), any())).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/users/{id}", 9999L)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));

        verify(userService, times(1)).update(eq(9999L), any());
    }

    @Test
    void deleteUserShouldDeleteSuccessfully() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(userId);
    }

    @Test
    void deleteUserWithWrongIdShouldThrowException() throws Exception {
        Long userId = 9999L;
        doThrow(new NotFoundException("Пользователь не найден")).when(userService).delete(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));

        verify(userService, times(1)).delete(userId);
    }

    private UserDto getUserDto(int count) {
        userCount++;
        return UserDto.builder()
                .id((long) count)
                .name("User" + count)
                .email("user" + count + "@mail.ru")
                .build();
    }
}