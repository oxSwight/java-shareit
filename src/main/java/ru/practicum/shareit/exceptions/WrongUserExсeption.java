package ru.practicum.shareit.exceptions;

public class WrongUserExсeption extends RuntimeException {
    public WrongUserExсeption(String message) {
        super(message);
    }
}