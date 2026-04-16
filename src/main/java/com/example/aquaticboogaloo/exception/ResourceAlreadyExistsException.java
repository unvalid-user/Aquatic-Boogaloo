package com.example.aquaticboogaloo.exception;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.RESOURCE_ALREADY_EXISTS;

public class ResourceAlreadyExistsException extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format(RESOURCE_ALREADY_EXISTS, resourceName, fieldName, fieldValue));

        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
