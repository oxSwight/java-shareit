package ru.practicum.shareit.exceptions;

public class WrongUserExeption extends RuntimeException {
    public WrongUserExeption(String message) {
        super(message);
    }
}