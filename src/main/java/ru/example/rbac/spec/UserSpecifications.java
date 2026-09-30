package ru.example.rbac.spec;

import ru.example.rbac.entity.Permission;
import ru.example.rbac.entity.Role;
import ru.example.rbac.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withFilters(String email, String role,
                                                  String permission, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.exists(hasRole(query, cb, root, role)));
            }
            if (permission != null && !permission.isBlank()) {
                predicates.add(cb.exists(hasPermission(query, cb, root, permission)));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Subquery<Long> hasRole(CriteriaQuery<?> query, CriteriaBuilder cb,
                                          Root<User> root, String roleName) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<User> subUser = sub.from(User.class);
        Join<User, Role> subRoles = subUser.join("roles");
        sub.select(subUser.get("id"))
                .where(cb.equal(subRoles.get("name"), roleName),
                        cb.equal(subUser.get("id"), root.get("id")));
        return sub;
    }

    private static Subquery<Long> hasPermission(CriteriaQuery<?> query, CriteriaBuilder cb,
                                                Root<User> root, String permissionCode) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<User> subUser = sub.from(User.class);
        Join<User, Role> subRoles = subUser.join("roles");
        Join<Role, Permission> subPerms = subRoles.join("permissions");
        sub.select(subUser.get("id"))
                .where(cb.equal(subPerms.get("code"), permissionCode),
                        cb.equal(subUser.get("id"), root.get("id")));
        return sub;
    }
}
