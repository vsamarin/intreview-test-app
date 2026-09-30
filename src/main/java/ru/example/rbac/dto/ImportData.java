package ru.example.rbac.dto;

import java.util.List;

/** Формат тестовых данных (db/import-data.json). */
public record ImportData(
        List<ImportRole> roles,
        List<ImportPermission> permissions,
        List<ImportUser> users) {

    public record ImportRole(String name, List<String> permissions) {
    }

    public record ImportPermission(String code, String description) {
    }

    public record ImportUser(String email, String name, boolean active, List<String> roles) {
    }
}
