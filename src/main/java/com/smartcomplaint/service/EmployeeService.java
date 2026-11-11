package com.smartcomplaint.service;

import com.smartcomplaint.dto.EmployeeRequest;
import com.smartcomplaint.model.Department;
import com.smartcomplaint.model.Employee;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.ComplaintAssignmentRepository;
import com.smartcomplaint.repository.DepartmentRepository;
import com.smartcomplaint.repository.EmployeeRepository;
import com.smartcomplaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           ComplaintAssignmentRepository assignmentRepository,
                           AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
        return employeeRepository.findByDepartment(dept);
    }

    @Transactional
    public Employee createEmployee(EmployeeRequest req) {
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + req.getDepartmentId()));

        String code = req.getEmployeeCode();
        if (code == null || code.isBlank()) {
            long count = employeeRepository.count() + 1;
            code = String.format("EMP-%s-%03d", dept.getCode(), count);
        } else {
            code = code.trim().toUpperCase();
            if (employeeRepository.findByEmployeeCode(code).isPresent()) {
                throw new IllegalArgumentException("Employee code " + code + " already exists");
            }
        }

        User user = null;
        if (req.getUserId() != null) {
            user = userRepository.findById(req.getUserId()).orElse(null);
        }

        Employee emp = new Employee();
        emp.setEmployeeCode(code);
        emp.setName(req.getName().trim());
        emp.setEmail(req.getEmail().trim().toLowerCase());
        emp.setPhone(req.getPhone());
        emp.setDepartment(dept);
        emp.setDesignation(req.getDesignation().trim());
        emp.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");
        emp.setSkills(req.getSkills());
        emp.setUser(user);
        emp.setJoinedDate(LocalDateTime.now());

        Employee saved = employeeRepository.save(emp);
        auditService.log(user, "CREATE_EMPLOYEE", "Employee:" + saved.getEmployeeCode(), null,
                "Employee " + saved.getName() + " (" + saved.getDesignation() + ") registered");
        return saved;
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeRequest req) {
        Employee emp = getEmployeeById(id);

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + req.getDepartmentId()));
            emp.setDepartment(dept);
        }

        if (req.getName() != null && !req.getName().isBlank()) {
            emp.setName(req.getName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            emp.setEmail(req.getEmail().trim().toLowerCase());
        }
        if (req.getPhone() != null) {
            emp.setPhone(req.getPhone());
        }
        if (req.getDesignation() != null && !req.getDesignation().isBlank()) {
            emp.setDesignation(req.getDesignation().trim());
        }
        if (req.getStatus() != null) {
            emp.setStatus(req.getStatus());
        }
        if (req.getSkills() != null) {
            emp.setSkills(req.getSkills());
        }
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId()).orElse(null);
            emp.setUser(user);
        }

        Employee updated = employeeRepository.save(emp);
        auditService.log(emp.getUser(), "UPDATE_EMPLOYEE", "Employee:" + updated.getEmployeeCode(), null,
                "Employee " + updated.getName() + " profile updated");
        return updated;
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee emp = getEmployeeById(id);
        employeeRepository.delete(emp);
        auditService.log(null, "DELETE_EMPLOYEE", "Employee:" + emp.getEmployeeCode(), null,
                "Employee " + emp.getName() + " removed");
    }
}

