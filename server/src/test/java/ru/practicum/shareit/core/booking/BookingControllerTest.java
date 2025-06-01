package ru.practicum.shareit.core.booking;

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
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingOutDto;
import ru.practicum.shareit.core.item.inside.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.inside.entity.dto.UserDto;
import ru.practicum.shareit.core.user.UserService;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.handlers.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createBookingShouldReturnCreatedBooking() throws Exception {
        Long userId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingInDto bookingInDto = createBookingInDto(1L, start, end);
        BookingOutDto bookingOutDto = createBookingOutDto(1L, 1L, userId, start, end);

        when(bookingService.create(any(), eq(userId))).thenReturn(bookingOutDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createBookingWithUnknownUserShouldReturnNotFound() throws Exception {
        Long userId = 999L;
        BookingInDto bookingInDto = createBookingInDto(1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        when(bookingService.create(any(), eq(userId))).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    void createBookingWithUnknownItemShouldReturnNotFound() throws Exception {
        Long userId = 1L;
        BookingInDto bookingInDto = createBookingInDto(999L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        when(bookingService.create(any(), eq(userId))).thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Предмет не найден"));
    }

    @Test
    void createBookingWithUnavailableItemShouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        BookingInDto bookingInDto = createBookingInDto(1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        when(bookingService.create(any(), eq(userId))).thenThrow(
                new ConditionsNotMetException("Предмет недоступен для аренды."));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Предмет недоступен для аренды."));
    }

    @Test
    void createBookingWithInvalidDatesShouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        BookingInDto bookingInDto = createBookingInDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now());

        when(bookingService.create(any(), eq(userId))).thenThrow(
                new ConditionsNotMetException("Время окончания бронирования должно быть после времени начала."));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Время окончания бронирования должно быть после времени начала."));
    }

    @Test
    void createBookingForItemOwnerShouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        BookingInDto bookingInDto = createBookingInDto(1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        when(bookingService.create(any(), eq(userId))).thenThrow(
                new ConditionsNotMetException("Владелец предмета не может арендовать его сам."));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Владелец предмета не может арендовать его сам."));
    }

    @Test
    void approveBookingShouldApproveBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 1L;
        BookingOutDto bookingOutDto = createBookingOutDto(bookingId, 1L, 2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingOutDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.updateStatus(bookingId, ownerId, true))
                .thenReturn(bookingOutDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rejectBookingShouldRejectBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 1L;
        BookingOutDto bookingOutDto = createBookingOutDto(bookingId, 1L, 2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingOutDto.setStatus(BookingStatus.REJECTED);

        when(bookingService.updateStatus(bookingId, ownerId, false))
                .thenReturn(bookingOutDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void updateStatusWithUnknownBookingShouldReturnNotFound() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 999L;

        when(bookingService.updateStatus(eq(bookingId), eq(ownerId), anyBoolean()))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void getBookingShouldReturnBooking() throws Exception {
        Long userId = 2L;
        Long bookingId = 1L;
        BookingOutDto bookingOutDto = createBookingOutDto(bookingId, 1L, userId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(bookingService.findById(bookingId, userId)).thenReturn(bookingOutDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getBookingWithUnknownBookingShouldReturnNotFound() throws Exception {
        Long userId = 1L;
        Long bookingId = 999L;

        when(bookingService.findById(bookingId, userId))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void getAllBookingsForUserShouldReturnBookings() throws Exception {
        Long userId = 2L;
        BookingOutDto booking1 = createBookingOutDto(1L, 1L, userId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingOutDto booking2 = createBookingOutDto(2L, 2L, userId,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4));

        when(bookingService.findAllByBookerAndState(BookingState.ALL, 2L))
                .thenReturn(List.of(booking1, booking2));
        when(userService.findById(anyLong())).thenReturn(any());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllBookingsForOwnerShouldReturnBookings() throws Exception {
        Long ownerId = 1L;
        BookingOutDto booking1 = createBookingOutDto(1L, 1L, 2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingOutDto booking2 = createBookingOutDto(2L, 2L, 3L,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4));

        when(bookingService.findAllByOwnerAndState(BookingState.ALL, 1L))
                .thenReturn(List.of(booking1, booking2));
        when(userService.findById(anyLong())).thenReturn(any());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private BookingInDto createBookingInDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingInDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
    }

    private BookingOutDto createBookingOutDto(Long bookingId, Long itemId, Long bookerId,
                                              LocalDateTime start, LocalDateTime end) {
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item " + itemId)
                .description("Description " + itemId)
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .id(bookerId)
                .name("User " + bookerId)
                .email("user" + bookerId + "@mail.ru")
                .build();

        return BookingOutDto.builder()
                .id(bookingId)
                .item(itemDto)
                .booker(userDto)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();
    }

}