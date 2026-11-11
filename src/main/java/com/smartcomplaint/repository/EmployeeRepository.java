package com.smartcomplaint.repository;

import com.smartcomplaint.model.Department;
import com.smartcomplaint.model.Employee;
import com.smartcomplaint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUser(User user);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartment(Department department);
    List<Employee> findByStatus(String status);
    long countByDepartment(Department department);
    long countByStatus(String status);
}

