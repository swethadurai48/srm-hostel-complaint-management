package com.smartcomplaint.repository;

import com.smartcomplaint.model.Approval;
import com.smartcomplaint.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByComplaintOrderByRequestedAtDesc(Complaint complaint);
    List<Approval> findByStatusOrderByRequestedAtDesc(String status);
    long countByStatus(String status);
}

