package ru.practicum.shareit.core.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.inside.entity.dto.BookingOutDto;
import ru.practicum.shareit.core.booking.inside.entity.model.Booking;
import ru.practicum.shareit.core.booking.inside.repository.BookingRepository;
import ru.practicum.shareit.core.item.inside.entity.model.Item;
import ru.practicum.shareit.core.item.inside.repository.ItemRepository;
import ru.practicum.shareit.core.user.inside.entity.model.User;
import ru.practicum.shareit.core.user.inside.repository.UserRepository;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime future = now.plusDays(1);
    private final LocalDateTime past = now.minusDays(1);

    private BookingInDto.BookingInDtoBuilder createBookingInDtoBuilder() {
        return BookingInDto.builder()
                .itemId(1L)
                .start(future)
                .end(future.plusDays(1));
    }

    private User createUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    private Item createItem(Long id, String name, User owner, Boolean available) {
        return Item.builder()
                .id(id)
                .name(name)
                .description("Description")
                .available(available)
                .owner(owner)
                .build();
    }

    private Booking createBooking(Long id, User booker, Item item, BookingStatus status,
                                  LocalDateTime start, LocalDateTime end) {
        return Booking.builder()
                .id(id)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build();
    }

    @Test
    void createShouldCreateBookingWhenAllConditionsMet() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingInDto bookingDto = createBookingInDtoBuilder().build();
        Booking booking = createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemIdAndEndIsAfterAndStartIsBefore(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);

        BookingOutDto result = bookingService.create(bookingDto, userId);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        verify(itemRepository).findById(anyLong());
        verify(userRepository).findById(userId);
        verify(bookingRepository).saveAndFlush(any(Booking.class));
    }

    @Test
    void createShouldThrowWhenItemNotFound() {
        Long userId = 1L;
        BookingInDto bookingDto = createBookingInDtoBuilder().build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, userId));
        verify(itemRepository).findById(anyLong());
        verifyNoInteractions(userRepository, bookingRepository);
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingInDto bookingDto = createBookingInDtoBuilder().build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, userId));
        verify(itemRepository).findById(anyLong());
        verify(userRepository).findById(userId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createShouldThrowWhenItemNotAvailable() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, false);
        BookingInDto bookingDto = createBookingInDtoBuilder().build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(bookingDto, userId));
        verify(itemRepository).findById(anyLong());
        verify(userRepository).findById(userId);
    }

    @Test
    void createShouldThrowWhenOwnerBooksOwnItem() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingInDto bookingDto = createBookingInDtoBuilder().build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(bookingDto, userId));
        verify(itemRepository).findById(anyLong());
        verify(userRepository).findById(userId);
    }

    @Test
    void createShouldThrowWhenItemAlreadyBooked() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingInDto bookingDto = createBookingInDtoBuilder().build();
        Booking existingBooking = createBooking(1L, booker, item, BookingStatus.APPROVED, future, future.plusDays(1));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemIdAndEndIsAfterAndStartIsBefore(anyLong(), any(), any()))
                .thenReturn(Optional.of(existingBooking));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(bookingDto, userId));
        verify(itemRepository).findById(anyLong());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByItemIdAndEndIsAfterAndStartIsBefore(anyLong(), any(), any());
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateStatusShouldApproveBooking() {
        Long bookingId = 1L;
        Long userId = 2L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(3L, "Booker", "booker@email.com");
        Item item = createItem(3L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);

        BookingOutDto result = bookingService.updateStatus(bookingId, userId, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void updateStatusShouldRejectBooking() {
        Long bookingId = 1L;
        Long userId = 2L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(3L, "Booker", "booker@email.com");
        Item item = createItem(3L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);

        BookingOutDto result = bookingService.updateStatus(bookingId, userId, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void updateStatusShouldThrowWhenBookingNotFound() {
        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.updateStatus(bookingId, userId, true));
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateStatusShouldThrowWhenUserIsNotOwner() {
        Long bookingId = 1L;
        Long userId = 2L;
        Long ownerId = 3L;
        User owner = createUser(ownerId, "Owner", "owner@email.com");
        User booker = createUser(4L, "Booker", "booker@email.com");
        Item item = createItem(3L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.updateStatus(bookingId, userId, true));
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void findByIdShouldReturnBookingWhenUserIsBooker() {
        Long bookingId = 1L;
        Long userId = 2L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(3L, "Owner", "owner@email.com");
        Item item = createItem(4L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        BookingOutDto result = bookingService.findById(bookingId, userId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        verify(bookingRepository).findById(bookingId);
        verify(userRepository).findById(userId);
    }

    @Test
    void findByIdShouldReturnBookingWhenUserIsOwner() {
        Long bookingId = 1L;
        Long bookerId = 2L;
        Long ownerId = 3L;
        User booker = createUser(bookerId, "Booker", "booker@email.com");
        User owner = createUser(ownerId, "Owner", "owner@email.com");
        Item item = createItem(4L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        BookingOutDto result = bookingService.findById(bookingId, ownerId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        verify(bookingRepository).findById(bookingId);
        verify(userRepository).findById(ownerId);
    }

    @Test
    void findByIdShouldThrowWhenUserNotRelatedToBooking() {
        Long bookingId = 1L;
        Long bookerId = 2L;
        Long ownerId = 3L;
        Long otherUserId = 4L;
        User booker = createUser(bookerId, "Booker", "booker@email.com");
        User owner = createUser(ownerId, "Owner", "owner@email.com");
        User otherUser = createUser(otherUserId, "Other", "other@email.com");
        Item item = createItem(4L, "Item", owner, true);
        Booking booking = createBooking(bookingId, booker, item, BookingStatus.WAITING, future, future.plusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.findById(bookingId, otherUserId));
        verify(bookingRepository).findById(bookingId);
        verify(userRepository).findById(otherUserId);
    }

    @Test
    void findAllByBookerAndStateShouldReturnAllBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.ALL;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1)),
                createBooking(2L, booker, item, BookingStatus.APPROVED, past, past.plusDays(1))
        );

        when(bookingRepository.findAllByBookerIdOrderByStartAsc(userId)).thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdOrderByStartAsc(userId);
    }

    @Test
    void findAllByBookerAndStateShouldReturnWaitingBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.WAITING;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
    }

    @Test
    void findAllByBookerAndStateShouldReturnRejectedBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.REJECTED;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.REJECTED, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
    }

    @Test
    void findAllByBookerAndStateShouldReturnPastBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.PAST;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.APPROVED, past, past.plusHours(1))
        );

        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(userId), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getEnd().isBefore(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void findAllByBookerAndStateShouldReturnCurrentBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.CURRENT;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.APPROVED, past, future)
        );

        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void findAllByBookerAndStateShouldReturnFutureBookings() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.FUTURE;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(eq(userId), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByBookerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getStart().isAfter(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByBookerIdAndStartAfterOrderByStartDesc(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void findAllByOwnerAndStateShouldReturnAllBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.ALL;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1)),
                createBooking(2L, booker, item, BookingStatus.APPROVED, past, past.plusDays(1))
        );

        when(bookingRepository.findAllByItemOwnerIdOrderByStartAsc(userId)).thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdOrderByStartAsc(userId);
    }

    @Test
    void findAllByOwnerAndStateShouldReturnWaitingBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.WAITING;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
    }

    @Test
    void findAllByOwnerAndStateShouldReturnRejectedBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.REJECTED;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.REJECTED, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
    }

    @Test
    void findAllByOwnerAndStateShouldReturnPastBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.PAST;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.APPROVED, past, past.plusHours(1))
        );

        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(userId), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getEnd().isBefore(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void findAllByOwnerAndStateShouldReturnCurrentBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.CURRENT;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.APPROVED, past, future)
        );

        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void findAllByOwnerAndStateShouldReturnFutureBookings() {
        Long userId = 1L;
        User owner = createUser(userId, "Owner", "owner@email.com");
        User booker = createUser(2L, "Booker", "booker@email.com");
        Item item = createItem(1L, "Item", owner, true);
        BookingState state = BookingState.FUTURE;

        List<Booking> bookings = List.of(
                createBooking(1L, booker, item, BookingStatus.WAITING, future, future.plusDays(1))
        );

        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(userId), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingOutDto> result = bookingService.findAllByOwnerAndState(state, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(bookings.get(0).getStart().isAfter(LocalDateTime.now()));
        assertEquals(item.getId(), result.get(0).getItem().getId());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void createShouldThrowWhenEndTimeBeforeStartTime() {
        Long userId = 1L;
        User booker = createUser(userId, "Booker", "booker@email.com");
        User owner = createUser(2L, "Owner", "owner@email.com");
        Item item = createItem(1L, "Item", owner, true);

        BookingInDto bookingDto = BookingInDto.builder()
                .itemId(1L)
                .start(future)
                .end(past)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemIdAndEndIsAfterAndStartIsBefore(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(bookingDto, userId));
    }
}