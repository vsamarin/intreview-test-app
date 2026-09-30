package com.example.rbac.service;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.exception.NotFoundException;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /** Идемпотентно: пара (role_id, permission_id) — PK join-таблицы. */
    @Transactional
    public void grantPermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("role not found: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("permission not found: " + permissionId));
        role.getPermissions().add(permission);
    }
}
