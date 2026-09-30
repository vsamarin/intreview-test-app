package ru.example.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка валидации одного поля")
public record FieldError(
        @Schema(description = "Имя поля", example = "email")
        String field,
        @Schema(description = "Сообщение об ошибке", example = "должно иметь формат адреса электронной почты")
        String message
) {
}
