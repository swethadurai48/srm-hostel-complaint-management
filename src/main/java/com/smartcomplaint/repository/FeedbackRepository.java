package com.smartcomplaint.repository;

import com.smartcomplaint.model.Feedback;
import com.smartcomplaint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStudentOrderByCreatedAtDesc(User student);
}
