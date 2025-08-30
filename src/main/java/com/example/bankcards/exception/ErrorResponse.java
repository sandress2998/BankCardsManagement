package com.example.bankcards.exception;

public class ErrorResponse {
    String message;
    int code;

    public ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() { return message; }

    public int getCode() { return code; }
}
