package com.swabhav.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swabhav.demo.dto.DepartmentRequestDto;
import com.swabhav.demo.dto.DepartmentResponseDto;
import com.swabhav.demo.dto.EmployeeRequestDto;
import com.swabhav.demo.dto.PageResponseDto;
import com.swabhav.demo.exception.DuplicateResourceException;
import com.swabhav.demo.exception.ResourceNotFoundException;
import com.swabhav.demo.model.Department;
import com.swabhav.demo.model.Employee;
import com.swabhav.demo.repository.DepartmentRepository;
import com.swabhav.demo.repository.EmployeeRepository;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, 
                                 EmployeeRepository employeeRepository, 
                                 ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequest) {
        logger.info("Executing createOperation Service Layer for: {}", departmentRequest.getDepartmentName());
        
        if (departmentRepository.existsByDepartmentName(departmentRequest.getDepartmentName())) {
            logger.warn("Duplicate department name found: {}", departmentRequest.getDepartmentName());
            throw new DuplicateResourceException("Department name already exists.");
        }
        
        validateEmployeeEmailsForCreate(departmentRequest.getEmployees());

        Department department = modelMapper.map(departmentRequest, Department.class);
        attachEmployeesToDepartment(department);

        Department savedDepartment = departmentRepository.save(department);
        return modelMapper.map(savedDepartment, DepartmentResponseDto.class);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments() {
        logger.info("Executing readOperation Service Layer for getAllDepartments");
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(dept -> modelMapper.map(dept, DepartmentResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDto<DepartmentResponseDto> getAllDepartmentsWithPagination(int pageNumber, int pageSize) {
        logger.info("Executing readOperation Service Layer with pagination: pageNumber={}, pageSize={}", pageNumber, pageSize);
        validatePagination(pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Department> departmentPage = departmentRepository.findAll(pageable);

        List<DepartmentResponseDto> content = departmentPage.getContent().stream()
                .map(dept -> modelMapper.map(dept, DepartmentResponseDto.class))
                .collect(Collectors.toList());

        PageResponseDto<DepartmentResponseDto> response = new PageResponseDto<>();
        response.setContent(content);
        response.setPageNumber(departmentPage.getNumber());
        response.setPageSize(departmentPage.getSize());
        response.setTotalElements(departmentPage.getTotalElements());
        response.setTotalPages(departmentPage.getTotalPages());
        response.setLastPage(departmentPage.isLast());

        return response;
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        logger.info("Executing readOperation Service Layer for getDepartmentById: {}", id);
        Department department = findDepartmentById(id);
        return modelMapper.map(department, DepartmentResponseDto.class);
    }

    @Override
    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto departmentRequest) {
        logger.info("Executing updateOperation Service Layer for department ID: {}", id);
        Department existingDepartment = findDepartmentById(id);

        if (departmentRepository.existsByDepartmentNameAndIdNot(departmentRequest.getDepartmentName(), id)) {
            logger.warn("Duplicate department name update attempt: {}", departmentRequest.getDepartmentName());
            throw new DuplicateResourceException("Another department with this name already exists.");
        }

        validateEmployeeEmailsForUpdate(departmentRequest.getEmployees(), id);

        existingDepartment.setDepartmentName(departmentRequest.getDepartmentName());
        existingDepartment.setLocation(departmentRequest.getLocation());

        existingDepartment.getEmployees().clear();
        
        Department updateContainer = modelMapper.map(departmentRequest, Department.class);
        if (updateContainer.getEmployees() != null) {
            existingDepartment.getEmployees().addAll(updateContainer.getEmployees());
        }
        attachEmployeesToDepartment(existingDepartment);

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return modelMapper.map(updatedDepartment, DepartmentResponseDto.class);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        logger.info("Executing deleteOperation Service Layer for department ID: {}", id);
        Department department = findDepartmentById(id);
        departmentRepository.delete(department);
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Resource not found: Department ID {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });
    }

    private void attachEmployeesToDepartment(Department department) {
        if (department.getEmployees() != null) {
            for (Employee employee : department.getEmployees()) {
                employee.setDepartment(department);
            }
        }
    }

    private void validateEmployeeEmailsForCreate(List<EmployeeRequestDto> employees) {
        for (EmployeeRequestDto empDto : employees) {
            if (employeeRepository.existsByEmail(empDto.getEmail())) {
                logger.warn("Duplicate employee email detected on creation: {}", empDto.getEmail());
                throw new DuplicateResourceException("Employee email already exists: " + empDto.getEmail());
            }
        }
    }

    private void validateEmployeeEmailsForUpdate(List<EmployeeRequestDto> employees, Long departmentId) {
        for (EmployeeRequestDto empDto : employees) {
            if (employeeRepository.existsByEmailAndDepartmentIdNot(empDto.getEmail(), departmentId)) {
                logger.warn("Duplicate employee email conflict on update: {}", empDto.getEmail());
                throw new DuplicateResourceException("Email conflict: " + empDto.getEmail() + " is assigned to an employee in a different department.");
            }
        }
    }

    private void validatePagination(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            logger.warn("Validation failure: Negative page number requested ({})", pageNumber);
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (pageSize <= 0) {
            logger.warn("Validation failure: Zero or negative page size requested ({})", pageSize);
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (pageSize > 100) {
            logger.warn("Validation failure: Page size exceeds limit ({})", pageSize);
            throw new IllegalArgumentException("Page size must not exceed 100");
        }
    }
}
