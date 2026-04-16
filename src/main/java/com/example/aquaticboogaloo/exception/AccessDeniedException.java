package com.example.aquaticboogaloo.exception;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.ACCESS_DENIED;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super(ACCESS_DENIED);
    }
}
