package com.smartcomplaint.repository;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintAssignment;
import com.smartcomplaint.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, Long> {
    List<ComplaintAssignment> findByComplaintOrderByAssignedAtDesc(Complaint complaint);
    List<ComplaintAssignment> findByEmployeeOrderByAssignedAtDesc(Employee employee);
    List<ComplaintAssignment> findByEmployeeAndStatus(Employee employee, String status);
    long countByEmployeeAndStatus(Employee employee, String status);
}

