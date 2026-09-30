package com.example.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Запрос на создание пользователя", example = """
        {
          "email": "newuser@example.com",
          "name": "Новый пользователь",
          "roleIds": [3],
          "active": true
        }
        """)
public record CreateUserRequest(
        @Schema(description = "Email (уникальный)", example = "newuser@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Имя, 2–100 символов", example = "Новый пользователь")
        @NotBlank @Size(min = 2, max = 100) String name,
        @Schema(description = "Существующие id ролей, минимум одна", example = "[3]")
        @NotEmpty List<Long> roleIds,
        @Schema(description = "Активность, по умолчанию true", example = "true")
        Boolean active
) {
}
