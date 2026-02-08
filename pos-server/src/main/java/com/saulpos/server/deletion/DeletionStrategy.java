package com.saulpos.server.deletion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface DeletionStrategy<T> {
    void delete(T entity, EntityManager em);

    Predicate getActivePredicate(CriteriaBuilder cb, Root<T> root);
}
