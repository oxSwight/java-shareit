package ru.practicum.shareit.handlers;

public class GlobalErrorResponse {
    private final String error;

    public GlobalErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}