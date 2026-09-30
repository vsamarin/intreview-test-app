package com.example.rbac.web;

import com.example.rbac.dto.CreateUserRequest;
import com.example.rbac.dto.PageResponse;
import com.example.rbac.dto.UserDto;
import com.example.rbac.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Список пользователей с фильтрацией и пагинацией",
            description = """
                    Все фильтры опциональны и комбинируются через AND:
                    * `email` — частичное совпадение, без учёта регистра (LIKE %...%)
                    * `role` — точное имя роли
                    * `permission` — код разрешения, доступный через любую роль пользователя
                    * `active` — true/false
                    * `page`/`size` — пагинация (по умолчанию 0/20)
                    * `sort` — `поле,направление`, например `email,asc` или `createdAt,desc`
                    """)
    @ApiResponse(responseCode = "200", description = "Страница пользователей")
    @ApiResponse(responseCode = "400", description = "Неверные параметры (например, неизвестное поле сортировки)")
    public PageResponse<UserDto> search(
            @Parameter(description = "Частичное совпадение email", example = "admin")
            @RequestParam(required = false) String email,
            @Parameter(description = "Точное имя роли", example = "ADMIN")
            @RequestParam(required = false) String role,
            @Parameter(description = "Код разрешения", example = "USER_READ")
            @RequestParam(required = false) String permission,
            @Parameter(description = "Активен ли пользователь", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка: поле,направление", example = "email,asc")
            @RequestParam(defaultValue = "email,asc") String sort) {
        return userService.search(email, role, permission, active, PageRequest.of(page, size, parseSort(sort)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пользователя",
            description = "Валидация: email — @NotBlank @Email, name — @NotBlank @Size(2..100), roleIds — @NotEmpty. "
                    + "Все roleIds должны существовать, иначе 400.")
    @ApiResponse(responseCode = "201", description = "Создан")
    @ApiResponse(responseCode = "400", description = "Ошибки валидации / неизвестные roleIds")
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
