package com.drystorm.api.repository;

import com.drystorm.api.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByActiveTrue();

    List<Service> findByCategoryAndActiveTrue(Service.ServiceCategory category);

    Optional<Service> findByIdAndActiveTrue(Long id);

    @Query("SELECT s FROM Service s WHERE s.active = true ORDER BY s.category, s.price")
    List<Service> findAllActiveOrderedByCategoryAndPrice();

    boolean existsByNameAndActiveTrue(String name);
}
