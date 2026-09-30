package ru.example.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Тело ответа об ошибках (400/404)")
public record ValidationErrorResponse(
        @Schema(description = "Список ошибок")
        List<FieldError> errors
) {
}
