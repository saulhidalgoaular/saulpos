package com.saulpos.server.deletion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.deletion-strategy", havingValue = "hard")
public class HardDeleteStrategy implements DeletionStrategy<Object> {
    @Override
    public void delete(Object entity, EntityManager em) {
        em.remove(entity);
    }

    @Override
    public Predicate getActivePredicate(CriteriaBuilder cb, Root<Object> root) {
        return cb.conjunction();
    }
}
