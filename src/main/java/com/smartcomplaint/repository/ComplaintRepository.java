package com.smartcomplaint.repository;

import com.smartcomplaint.model.Category;
import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Student queries
    List<Complaint> findByStudentOrderByCreatedAtDesc(User student);

    long countByStudent(User student);

    long countByStudentAndStatus(User student, ComplaintStatus status);

    // Admin & Global queries
    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findByCategoryOrderByCreatedAtDesc(Category category);

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    List<Complaint> findByCategoryAndStatusOrderByCreatedAtDesc(Category category, ComplaintStatus status);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    List<Complaint> findFilteredComplaints(@Param("category") Category category,
                                          @Param("status") ComplaintStatus status);

    long countByStatus(ComplaintStatus status);
}
