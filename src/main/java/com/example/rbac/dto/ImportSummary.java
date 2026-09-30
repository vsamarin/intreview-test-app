package com.example.rbac.dto;

/** Число сущностей после загрузки тестовых данных. */
public record ImportSummary(int users, int roles, int permissions) {
}
