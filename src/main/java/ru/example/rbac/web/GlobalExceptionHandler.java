package ru.example.rbac.web;

import ru.example.rbac.dto.FieldError;
import ru.example.rbac.dto.ValidationErrorResponse;
import ru.example.rbac.exception.BadRequestException;
import ru.example.rbac.exception.NotFoundException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return new ValidationErrorResponse(errors);
    }

    @ExceptionHandler({BadRequestException.class, InvalidDataAccessApiUsageException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleBadRequest(Exception ex) {
        return new ValidationErrorResponse(List.of(new FieldError("request", ex.getMessage())));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ValidationErrorResponse handleNotFound(NotFoundException ex) {
        return new ValidationErrorResponse(List.of(new FieldError("resource", ex.getMessage())));
    }
}
