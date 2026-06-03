package com.swabhav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.swabhav.demo.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    boolean existsByEmailAndDepartmentIdNot(String email, Long departmentId);
}
