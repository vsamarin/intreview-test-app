package ru.example.rbac.web;

import ru.example.rbac.dto.ValidationErrorResponse;
import ru.example.rbac.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Выдать разрешение роли",
            description = "Идемпотентно: пара (role_id, permission_id) — первичный ключ join-таблицы, "
                    + "повторная выдача не создаёт дубль.")
    @ApiResponse(responseCode = "204", description = "Выдано")
    @ApiResponse(responseCode = "404", description = "Роль или разрешение не найдены",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public void grantPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.grantPermission(roleId, permissionId);
    }
}
