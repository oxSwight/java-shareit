package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingEvent;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatusType;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.ExceptionMessages;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.WrongUserException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StateMachineFactory<BookingStatusType, BookingEvent> stateMachineFactory;

    // Вспомогательный метод для создания сортировки
    private Sort createSort(BookingState state) {
        return state == BookingState.ALL
                ? Sort.by(Sort.Direction.ASC, "start")
                : Sort.by(Sort.Direction.DESC, "start");
    }

    public BookingDto getBookingDto(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = getBooking(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.BOOKING_GET_INFO_ERROR);
        }

        Booking booking = bookingOptional.get();
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException(ExceptionMessages.BOOKING_GET_INFO_ERROR);
        }
        return BookingMapper.toBookingDto(booking);
    }

    public List<BookingDto> readByBookerAndState(BookingState state, Long userId) {
        Sort sort = createSort(state);
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerId(userId, sort);
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(
                        userId,
                        BookingStatusType.valueOf(state.name()),
                        sort
                );
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(
                        userId,
                        LocalDateTime.now(),
                        sort
                );
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        sort
                );
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(
                        userId,
                        LocalDateTime.now(),
                        sort
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    public List<BookingDto> readByOwnerAndState(BookingState state, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.USER_NOT_FOUND_ERROR, userId)));

        Sort sort = createSort(state);
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerId(userId, sort);
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(
                        userId,
                        BookingStatusType.valueOf(state.name()),
                        sort
                );
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndBefore(
                        userId,
                        LocalDateTime.now(),
                        sort
                );
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        sort
                );
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartAfter(
                        userId,
                        LocalDateTime.now(),
                        sort
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    public BookingDto createBooking(BookingDto bookingDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.USER_NOT_FOUND_ERROR, userId)));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format(ExceptionMessages.ITEM_NOT_FOUND_ERROR, bookingDto.getItemId())));

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        validateBooking(booking);
        booking = bookingRepository.saveAndFlush(booking);

        return BookingMapper.toBookingDto(booking);
    }

    public BookingDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        Optional<Booking> bookingOptional = getBooking(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.BOOKING_GET_INFO_ERROR);
        }

        Booking booking = bookingOptional.get();
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new WrongUserException(ExceptionMessages.BOOKING_CHANGE_STATUS);
        }

        changeStatus(booking, approved ? BookingEvent.APPROVE : BookingEvent.REJECT);

        return BookingMapper.toBookingDto(bookingRepository.saveAndFlush(booking));
    }

    public BookingDto cancelBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = getBooking(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.BOOKING_GET_INFO_ERROR);
        }

        Booking booking = bookingOptional.get();

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException(ExceptionMessages.BOOKING_CHANGE_STATUS);
        }

        changeStatus(booking, BookingEvent.CANCEL);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    private void validateBooking(Booking booking) {
        Optional<Booking> bookingOptional = bookingRepository.findByItemIdAndEndIsAfterAndStartIsBefore(
                booking.getItem().getId(),
                booking.getEnd(),
                booking.getStart()
        );

        if (bookingOptional.isPresent()) {
            throw new ConditionsNotMetException(ExceptionMessages.BOOKING_ITEM_IS_BOOKED);
        }

        Item item = booking.getItem();

        if (!item.getIsAvailable()) {
            throw new ValidationException(ExceptionMessages.BOOKING_ITEM_IS_NOT_AVAILABLE);
        }

        if (booking.getBooker().getId().equals(item.getOwner().getId())) {
            throw new ValidationException(ExceptionMessages.BOOKING_OWNER_CANT_BOOKED);
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new ValidationException(ExceptionMessages.BOOKING_END_BEFORE_START);
        }
    }

    private Optional<Booking> getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .or(() -> {
                    throw new NotFoundException(String.format(ExceptionMessages.BOOKING_NOT_FOUND_ERROR, bookingId));
                });
    }

    private void changeStatus(Booking booking, BookingEvent bookingEvent) {
        StateMachine<BookingStatusType, BookingEvent> sm = stateMachineFactory.getStateMachine(booking.getId().toString());
        sm.start();
        sm.getExtendedState().getVariables().put("booking", booking);
        sm.sendEvent(bookingEvent);

        if (sm.getState() == null) {
            throw new IllegalStateException("StateMachine is not properly initialized or started.");
        }

        booking.setStatus(sm.getState().getId());
        sm.stop();
    }
}