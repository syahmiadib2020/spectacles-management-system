package com.meteor.eyewear;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

interface Accounts extends JpaRepository<Account, Long> {
    Optional<Account> findByEmailIgnoreCase(String email);

    Optional<Account> findByIc(String ic);

    List<Account> findByRoleOrderByNameAsc(String role);
}

interface Products extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id=:id")
    Optional<Product> locked(@Param("id") Long id);
}

interface Profiles extends JpaRepository<SpectacleProfile, Long> {
    Optional<SpectacleProfile> findByCustomerId(Long id);
}

interface Orders extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.id=:id")
    Optional<CustomerOrder> locked(@Param("id") Long id);
}

interface Events extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findByOrderIdOrderByCreatedAtAsc(Long id);
}
