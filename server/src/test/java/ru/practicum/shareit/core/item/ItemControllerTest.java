package ru.practicum.shareit.core.item;

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
import ru.practicum.shareit.core.item.inside.entity.dto.CommentDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.core.user.UserService;
import ru.practicum.shareit.handlers.ErrorHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createItemShouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = getItemDto(1);

        when(itemService.create(any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));
    }

    @Test
    void findByIdShouldReturnItem() throws Exception {
        ItemDto itemDto = getItemDto(1);
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.findById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));
    }

    @Test
    void findByIdWithWrongIdShouldReturnNotFound() throws Exception {
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.findById(999L)).thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(get("/items/{id}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Предмет не найден"));
    }

    @Test
    void findAllOwnedShouldReturnItems() throws Exception {
        UserDto userDto = getUserDto(1);
        ItemDto item1 = getItemDto(1);
        ItemDto item2 = getItemDto(2);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.findAllOwned(1L)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAllOwnedWithUnknownUserShouldReturnNotFound() throws Exception {
        when(userService.findById(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void updateShouldReturnUpdatedItem() throws Exception {
        ItemDto itemDto = getItemDto(1);
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.update(anyLong(), any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));
    }

    @Test
    void updateWithWrongItemIdShouldReturnNotFound() throws Exception {
        ItemDto itemDto = getItemDto(1);
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.update(anyLong(), any(), anyLong())).thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(patch("/items/{id}", 999L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Предмет не найден"));
    }

    @Test
    void updateWithWrongUserIdShouldReturnNotFound() throws Exception {
        ItemDto itemDto = getItemDto(1);

        when(userService.findById(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 999L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void updateWithNotOwnerShouldReturnConditionsNotMet() throws Exception {
        ItemDto itemDto = getItemDto(1);
        UserDto userDto = getUserDto(2);

        when(userService.findById(2L)).thenReturn(userDto);
        when(itemService.update(anyLong(), any(), anyLong())).thenThrow(new ConditionsNotMetException("Пользователь не владелец предмета"));

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Пользователь не владелец предмета"));
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        UserDto userDto = getUserDto(1);
        ItemDto item1 = getItemDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.search("SearchItem")).thenReturn(List.of(item1));

        mockMvc.perform(get("/items/search")
                        .param("text", "SearchItem")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchWithEmptyQueryShouldReturnEmptyList() throws Exception {
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createCommentShouldReturnComment() throws Exception {
        CommentDto commentDto = getCommentDto("Comment");
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.createComment(1L, commentDto, 1L)).thenReturn(commentDto);

        mockMvc.perform(post("/items/{id}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Comment"));
    }

    @Test
    void createCommentWithUnknownUserShouldReturnNotFound() throws Exception {
        CommentDto commentDto = getCommentDto("Comment");

        when(userService.findById(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items/{id}/comment", 1L)
                        .header("X-Sharer-User-Id", 999L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void createCommentWithUnknownItemShouldReturnNotFound() throws Exception {
        CommentDto commentDto = getCommentDto("Comment");
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.createComment(999L, commentDto, 1L)).thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(post("/items/{id}/comment", 999L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Предмет не найден"));
    }

    @Test
    void createCommentWithNotBookedItemShouldReturnConditionsNotMet() throws Exception {
        CommentDto commentDto = getCommentDto("Comment");
        UserDto userDto = getUserDto(1);

        when(userService.findById(1L)).thenReturn(userDto);
        when(itemService.createComment(1L, commentDto, 1L)).thenThrow(
                new ConditionsNotMetException("Пользователь не арендовал предмет или время аренды еще не вышло"));

        mockMvc.perform(post("/items/{id}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Пользователь не арендовал предмет или время аренды еще не вышло"));
    }

    private ItemDto getItemDto(int count) {
        return ItemDto.builder()
                .id((long) count)
                .name("Item" + count)
                .description("Description" + count)
                .available(true)
                .build();
    }

    private UserDto getUserDto(int count) {
        return UserDto.builder()
                .id((long) count)
                .name("User" + count)
                .email("user" + count + "@mail.ru")
                .build();
    }

    private CommentDto getCommentDto(String text) {
        return CommentDto.builder()
                .text(text)
                .build();
    }
}