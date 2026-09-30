package ru.example.rbac.repository;

import ru.example.rbac.entity.Role;
import ru.example.rbac.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JpaSpecificationExecutor не поддерживает fetch-join, поэтому выборка
 * собрана вручную в два этапа: fetch-join несовместим с setFirstResult/setMaxResults
 * (Hibernate применяет лимит в памяти, HHH90003004), поэтому LIMIT/OFFSET
 * выполняется на уровне БД по id, а граф поднимается только для страницы.
 */
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final EntityManager em;

    public UserRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<User> findWithGraph(Specification<User> spec, Pageable pageable) {
        List<Long> ids = pageIds(spec, pageable);
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }
        List<User> users = fetchGraph(ids);
        return new PageImpl<>(users, pageable, count(spec));
    }

    /** Этап 1: страница id с фильтрами и сортировкой, LIMIT/OFFSET — в БД. */
    private List<Long> pageIds(Specification<User> spec, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);
        cq.select(root.get("id"));

        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.orderBy(toOrders(root, cb, pageable));

        TypedQuery<Long> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    /** Этап 2: граф User → roles → permissions одним LEFT JOIN, без пагинации. */
    private List<User> fetchGraph(List<Long> ids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root);

        Fetch<User, Role> roles = root.fetch("roles", JoinType.LEFT);
        roles.fetch("permissions", JoinType.LEFT);

        cq.where(root.get("id").in(ids));
        cq.distinct(true);

        Map<Long, User> byId = new HashMap<>();
        for (User user : em.createQuery(cq).getResultList()) {
            byId.put(user.getId(), user);
        }
        // порядок страницы — порядок id из этапа 1
        return ids.stream().map(byId::get).toList();
    }

    private long count(Specification<User> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }

    private Order[] toOrders(Root<User> root, CriteriaBuilder cb, Pageable pageable) {
        return pageable.getSort().stream()
                .map(o -> o.isAscending() ? cb.asc(root.get(o.getProperty())) : cb.desc(root.get(o.getProperty())))
                .toArray(Order[]::new);
    }
}
