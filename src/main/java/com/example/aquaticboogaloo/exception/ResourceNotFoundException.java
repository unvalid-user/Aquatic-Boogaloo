package com.example.aquaticboogaloo.exception;

import lombok.Getter;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.RESOURCE_NOT_FOUND;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format(RESOURCE_NOT_FOUND, resourceName, fieldName, fieldValue));

        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
