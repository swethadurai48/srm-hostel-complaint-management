package com.smartcomplaint.service;

import com.smartcomplaint.model.AuditLog;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User user, String action, String resource, String ipAddress, String details) {
        AuditLog entry = new AuditLog(user, action, resource, ipAddress, details);
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}

