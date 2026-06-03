package com.swabhav.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.swabhav.demo.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDepartmentName(String departmentName);
        
    boolean existsByDepartmentNameAndIdNot(String departmentName, Long id);
}
