package com.example.aquaticboogaloo.exception;

import com.example.aquaticboogaloo.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.HibernateError;
import org.hibernate.HibernateException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.example.aquaticboogaloo.exception.ExceptionMessage.ARGUMENT_TYPE_MISMATCH;
import static com.example.aquaticboogaloo.exception.ExceptionMessage.METHOD_NOT_SUPPORTED;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ErrorResponse resolveNotFoundException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ResourceAlreadyExistsException.class, ConflictException.class})
    public ErrorResponse resolveException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse resolveException(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse resolveException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResponse resolveException(
            UnauthorizedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse resolveException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<String> messages = new ArrayList<>(fieldErrors.size());
        for (FieldError error: fieldErrors) {
            messages.add(String.format("%s: %s",error.getField(), error.getDefaultMessage()));
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                messages,
                request
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse resolveException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = String.format(
                ARGUMENT_TYPE_MISMATCH,
                exception.getParameter().getParameterName(),
                exception.getRequiredType() == null ? "?" : exception.getRequiredType().getSimpleName()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                List.of(message),
                request
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyReferenceException.class)
    public ErrorResponse resolveException(
            PropertyReferenceException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse resolveException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null,
                request
        );
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse resolveException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        String message = String.format(METHOD_NOT_SUPPORTED, exception.getMethod());

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                exception.getMessage(),
                List.of(message),
                request
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({HibernateException.class, SQLException.class})
    public ErrorResponse resolveSqlException(
            Exception exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                List.of("SQL or Hibernate exception"),
                request
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAllExceptions(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                null,
                request
        );
    }


    private ErrorResponse buildErrorResponse(HttpStatus status, String message, List<String> details, HttpServletRequest request) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .details(details)
                .path(request.getRequestURI())
                .build();
    }
}
