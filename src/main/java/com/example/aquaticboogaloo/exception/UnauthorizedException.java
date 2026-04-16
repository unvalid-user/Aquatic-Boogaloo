package com.example.aquaticboogaloo.exception;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.UNAUTHORIZED;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super(UNAUTHORIZED);
    }
}
