package com.saulpos.server.customer.repository;

import com.saulpos.api.customer.CustomerContactType;
import com.saulpos.server.customer.model.CustomerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    @EntityGraph(attributePaths = {"merchant", "taxIdentities", "contacts", "groupAssignments", "groupAssignments.customerGroup"})
    @Query("select c from CustomerEntity c where c.id = :id")
    Optional<CustomerEntity> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"merchant", "taxIdentities", "contacts", "groupAssignments", "groupAssignments.customerGroup"})
    @Query("""
            select distinct c from CustomerEntity c
            where (:merchantId is null or c.merchant.id = :merchantId)
              and (:active is null or c.active = :active)
            order by c.id asc
            """)
    List<CustomerEntity> search(
            @Param("merchantId") Long merchantId,
            @Param("active") Boolean active);

    @EntityGraph(attributePaths = {"merchant", "taxIdentities", "contacts", "groupAssignments", "groupAssignments.customerGroup"})
    @Query("""
            select distinct c from CustomerEntity c
            join c.taxIdentities ti
            where c.merchant.id = :merchantId
              and c.active = true
              and ti.active = true
              and ti.documentType = :documentType
              and ti.documentValueNormalized = :documentValueNormalized
            order by c.id asc
            """)
    List<CustomerEntity> findActiveByDocument(
            @Param("merchantId") Long merchantId,
            @Param("documentType") String documentType,
            @Param("documentValueNormalized") String documentValueNormalized);

    @EntityGraph(attributePaths = {"merchant", "taxIdentities", "contacts", "groupAssignments", "groupAssignments.customerGroup"})
    @Query("""
            select distinct c from CustomerEntity c
            join c.contacts cc
            where c.merchant.id = :merchantId
              and c.active = true
              and cc.active = true
              and cc.contactType = :contactType
              and cc.contactValueNormalized = :contactValueNormalized
            order by c.id asc
            """)
    List<CustomerEntity> findActiveByContact(
            @Param("merchantId") Long merchantId,
            @Param("contactType") CustomerContactType contactType,
            @Param("contactValueNormalized") String contactValueNormalized);
}
