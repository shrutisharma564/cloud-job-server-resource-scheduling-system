package com.cloudscheduler.repository;
import com.cloudscheduler.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface AllocationRepository extends JpaRepository<Allocation,Long> {
    List<Allocation> findByStatus(String status);
    Optional<Allocation> findFirstByJob_IdAndStatus(Long jobId, String status);
}
