package ru.example.rbac.repository;

import ru.example.rbac.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, UserRepositoryCustom {

    /**
     * Фильтрация по Specification + одна выборка с JOIN FETCH ролей и разрешений
     * (защита от N+1, см. UserRepositoryImpl).
     */
    Page<User> findWithGraph(Specification<User> spec, Pageable pageable);

    Optional<User> findByEmail(String email);
}
