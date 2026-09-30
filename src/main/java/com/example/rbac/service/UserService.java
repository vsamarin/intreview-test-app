package com.example.rbac.service;

import com.example.rbac.dto.CreateUserRequest;
import com.example.rbac.dto.PageResponse;
import com.example.rbac.dto.UserDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.exception.BadRequestException;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import com.example.rbac.spec.UserSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> search(String email, String role, String permission,
                                        Boolean active, Pageable pageable) {
        return PageResponse.from(userRepository
                .findWithGraph(UserSpecifications.withFilters(email, role, permission, active), pageable)
                .map(this::toDto));
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        List<Role> roles = roleRepository.findAllById(request.roleIds());
        if (roles.size() != request.roleIds().size()) {
            throw new BadRequestException("roleIds: unknown role id");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setActive(request.active() == null || request.active());
        user.getRoles().addAll(roles);

        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.isActive(),
                u.getRoles().stream().map(Role::getName).sorted().toList(),
                u.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getCode)
                        .distinct()
                        .sorted()
                        .toList());
    }
}
