package ru.example.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Пользователь со своими ролями и разрешениями (сумма разрешений ролей)", example = """
        {
          "id": 1,
          "email": "admin@example.com",
          "name": "Администратор",
          "active": true,
          "roles": ["ADMIN"],
          "permissions": ["PERMISSION_READ", "ROLE_READ", "ROLE_WRITE", "USER_READ", "USER_WRITE"]
        }
        """)
public record UserDto(
        @Schema(description = "Идентификатор", example = "1", readOnly = true)
        Long id,
        @Schema(description = "Email (уникальный)", example = "admin@example.com")
        String email,
        @Schema(description = "Имя", example = "Администратор")
        String name,
        @Schema(description = "Активен ли пользователь", example = "true")
        boolean active,
        @Schema(description = "Имена ролей", example = "[\"ADMIN\"]")
        List<String> roles,
        @Schema(description = "Коды разрешений, собранные из всех ролей: без дублей, отсортированы",
                example = "[\"PERMISSION_READ\",\"ROLE_READ\",\"ROLE_WRITE\",\"USER_READ\",\"USER_WRITE\"]")
        List<String> permissions
) {
}
