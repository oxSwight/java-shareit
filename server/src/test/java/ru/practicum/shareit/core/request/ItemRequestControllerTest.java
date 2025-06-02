package ru.practicum.shareit.core.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemShortDto;
import ru.practicum.shareit.core.request.inside.entity.dto.ItemRequestDto;
import ru.practicum.shareit.core.user.UserService;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.handlers.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private ItemRequestDto itemRequestDto;
    private ItemDto itemDto;
    private ItemShortDto itemShortDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemRequestController)
                .setControllerAdvice(new ErrorHandler())
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Name")
                .email("email@email.ru")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need item for testing")
                .created(LocalDateTime.now())
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        itemShortDto = ItemShortDto.builder()
                .id(1L)
                .name("Item")
                .ownerId(1L)
                .build();
    }

    @Test
    void createShouldCreateNewRequest() throws Exception {
        when(itemRequestService.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    void findAllOwnShouldReturnUserRequests() throws Exception {
        when(itemRequestService.findAllOwn(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void findByIdShouldReturnRequestWithItems() throws Exception {
        itemRequestDto.setItems(List.of(itemShortDto));

        when(itemRequestService.findById(anyLong()))
                .thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(itemDto.getId()), Long.class));
    }

    @Test
    void findAllShouldReturnOtherUsersRequests() throws Exception {
        when(itemRequestService.findAll(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void findByIdShouldThrowWhenRequestNotFound() throws Exception {
        when(itemRequestService.findById(anyLong()))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}