package ru.example.rbac.service;

import ru.example.rbac.dto.ImportData;
import ru.example.rbac.dto.ImportSummary;
import ru.example.rbac.entity.Permission;
import ru.example.rbac.entity.Role;
import ru.example.rbac.entity.User;
import ru.example.rbac.repository.PermissionRepository;
import ru.example.rbac.repository.RoleRepository;
import ru.example.rbac.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Загружает тестовые данные из db/import-data.json. Идемпотентно:
 * существующие сущности ищутся по уникальному ключу, отсутствующие
 * создаются, связи синхронизируются с файлом.
 */
@Service
public class ImportService {

    private static final String IMPORT_RESOURCE = "db/import-data.json";

    private final ObjectMapper objectMapper;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public ImportService(ObjectMapper objectMapper,
                       PermissionRepository permissionRepository,
                       RoleRepository roleRepository,
                       UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ImportSummary load() {
        ImportData data = readImportData();

        Map<String, Permission> permissions = new HashMap<>();
        for (ImportData.ImportPermission item : data.permissions()) {
            Permission permission = permissionRepository.findByCode(item.code())
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setCode(item.code());
                        return permissionRepository.save(p);
                    });
            permission.setDescription(item.description());
            permissions.put(item.code(), permission);
        }

        Map<String, Role> roles = new HashMap<>();
        for (ImportData.ImportRole item : data.roles()) {
            Role role = roleRepository.findByName(item.name())
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(item.name());
                        return roleRepository.save(r);
                    });
            role.getPermissions().clear();
            for (String code : item.permissions()) {
                Permission permission = permissions.get(code);
                if (permission == null) {
                    throw new IllegalStateException("Неизвестный код разрешения: " + code);
                }
                role.getPermissions().add(permission);
            }
            roles.put(item.name(), role);
        }

        for (ImportData.ImportUser item : data.users()) {
            User user = userRepository.findByEmail(item.email())
                    .orElseGet(() -> {
                        User u = new User();
                        u.setEmail(item.email());
                        return userRepository.save(u);
                    });
            user.setName(item.name());
            user.setActive(item.active());
            user.getRoles().clear();
            for (String name : item.roles()) {
                Role role = roles.get(name);
                if (role == null) {
                    throw new IllegalStateException("Неизвестная роль: " + name);
                }
                user.getRoles().add(role);
            }
        }

        return new ImportSummary(data.users().size(), data.roles().size(), data.permissions().size());
    }

    private ImportData readImportData() {
        try {
            return objectMapper.readValue(new ClassPathResource(IMPORT_RESOURCE).getInputStream(), ImportData.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + IMPORT_RESOURCE, e);
        }
    }
}
