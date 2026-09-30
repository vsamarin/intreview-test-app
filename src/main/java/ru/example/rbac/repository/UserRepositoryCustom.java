package ru.example.rbac.repository;

import ru.example.rbac.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserRepositoryCustom {

    Page<User> findWithGraph(Specification<User> spec, Pageable pageable);
}
