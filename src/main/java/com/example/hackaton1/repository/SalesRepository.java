package com.example.hackaton1.repository;

import com.example.hackaton1.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sale, String> {

    Page<Sale> findByBranch(String branch, Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE s.soldAt BETWEEN :from AND :to")
    List<Sale> findByDateRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT s FROM Sale s WHERE s.soldAt BETWEEN :from AND :to AND s.branch = :branch")
    List<Sale> findByDateRangeAndBranch(@Param("from") Instant from, @Param("to") Instant to, @Param("branch") String branch);

    @Query("SELECT s FROM Sale s WHERE s.soldAt BETWEEN :from AND :to AND (:branch IS NULL OR s.branch = :branch)")
    Page<Sale> findWithFilters(@Param("from") Instant from, @Param("to") Instant to, @Param("branch") String branch, Pageable pageable);
}