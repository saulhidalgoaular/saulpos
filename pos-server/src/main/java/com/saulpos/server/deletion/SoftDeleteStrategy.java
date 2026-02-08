package com.saulpos.server.deletion;

import com.saulpos.core.deletion.SoftDeletable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(name = "app.deletion-strategy", havingValue = "soft", matchIfMissing = true)
public class SoftDeleteStrategy implements DeletionStrategy<Object> {

    @Override
    public void delete(Object entity, EntityManager em) {
        if (entity instanceof SoftDeletable) {
            SoftDeletable softDeletable = (SoftDeletable) entity;
            softDeletable.setDeleted(true);
            softDeletable.setDeletedAt(LocalDateTime.now());
            // TODO: Inject context to setDeletedBy
            em.merge(entity);
        } else {
            em.remove(entity);
        }
    }

    @Override
    public Predicate getActivePredicate(CriteriaBuilder cb, Root<Object> root) {
        if (SoftDeletable.class.isAssignableFrom(root.getJavaType())) {
            return cb.equal(root.get("deleted"), false);
        }
        return cb.conjunction();
    }
}
