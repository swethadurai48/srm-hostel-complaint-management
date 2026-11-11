package com.smartcomplaint.repository;

import com.smartcomplaint.model.Notification;
import com.smartcomplaint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndIsReadFalse(User recipient);
}

