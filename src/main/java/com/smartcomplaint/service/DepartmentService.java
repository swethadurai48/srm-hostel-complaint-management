package com.smartcomplaint.service;

import com.smartcomplaint.dto.DepartmentRequest;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.Department;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.ComplaintRepository;
import com.smartcomplaint.repository.DepartmentRepository;
import com.smartcomplaint.repository.EmployeeRepository;
import com.smartcomplaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ComplaintRepository complaintRepository;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             EmployeeRepository employeeRepository,
                             ComplaintRepository complaintRepository,
                             AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.complaintRepository = complaintRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(DepartmentRequest req) {
        if (departmentRepository.findByCode(req.getCode().trim().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Department with code " + req.getCode() + " already exists");
        }

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId()).orElse(null);
        }

        Department dept = new Department();
        dept.setCode(req.getCode().trim().toUpperCase());
        dept.setName(req.getName().trim());
        dept.setDescription(req.getDescription());
        dept.setManager(manager);
        dept.setContactEmail(req.getContactEmail());
        dept.setContactPhone(req.getContactPhone());
        dept.setBudget(req.getBudget() != null ? req.getBudget() : 0.0);

        Department saved = departmentRepository.save(dept);
        auditService.log(manager, "CREATE_DEPARTMENT", "Department:" + saved.getCode(), null,
                "Department " + saved.getName() + " created");
        return saved;
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest req) {
        Department dept = getDepartmentById(id);

        if (req.getCode() != null && !req.getCode().isBlank()) {
            String newCode = req.getCode().trim().toUpperCase();
            departmentRepository.findByCode(newCode).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Department with code " + newCode + " already exists");
                }
            });
            dept.setCode(newCode);
        }

        if (req.getName() != null && !req.getName().isBlank()) {
            dept.setName(req.getName().trim());
        }
        if (req.getDescription() != null) {
            dept.setDescription(req.getDescription());
        }
        if (req.getManagerId() != null) {
            User manager = userRepository.findById(req.getManagerId()).orElse(null);
            dept.setManager(manager);
        }
        if (req.getContactEmail() != null) {
            dept.setContactEmail(req.getContactEmail());
        }
        if (req.getContactPhone() != null) {
            dept.setContactPhone(req.getContactPhone());
        }
        if (req.getBudget() != null) {
            dept.setBudget(req.getBudget());
        }

        Department updated = departmentRepository.save(dept);
        auditService.log(dept.getManager(), "UPDATE_DEPARTMENT", "Department:" + updated.getCode(), null,
                "Department " + updated.getName() + " updated");
        return updated;
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        long employeeCount = employeeRepository.countByDepartment(dept);
        if (employeeCount > 0) {
            throw new IllegalStateException("Cannot delete department with " + employeeCount + " assigned employees. Reassign employees first.");
        }
        departmentRepository.delete(dept);
        auditService.log(null, "DELETE_DEPARTMENT", "Department:" + dept.getCode(), null,
                "Department " + dept.getName() + " deleted");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentStats(Long id) {
        Department dept = getDepartmentById(id);
        Map<String, Object> stats = new HashMap<>();

        long totalComplaints = complaintRepository.countByDepartment(dept);
        long pending = complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.RESOLVED);
        long employees = employeeRepository.countByDepartment(dept);

        stats.put("department", dept);
        stats.put("totalComplaints", totalComplaints);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        stats.put("employeeCount", employees);
        stats.put("budget", dept.getBudget());

        return stats;
    }
}

