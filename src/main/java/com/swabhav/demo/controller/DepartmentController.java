package com.swabhav.demo.controller;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.swabhav.demo.dto.DepartmentRequestDto;
import com.swabhav.demo.dto.DepartmentResponseDto;
import com.swabhav.demo.dto.PageResponseDto;
import com.swabhav.demo.service.DepartmentService;

@RestController
@RequestMapping("/api/departments")
@Validated 
public class DepartmentController {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto departmentRequest) {
        logger.info("API request received: POST /api/departments");
        return new ResponseEntity<>(departmentService.createDepartment(departmentRequest), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        logger.info("API request received: GET /api/departments");
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDto<DepartmentResponseDto>> getAllDepartmentsWithPagination(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize) {
        logger.info("API request received: GET /api/departments/page");
        return ResponseEntity.ok(departmentService.getAllDepartmentsWithPagination(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable("id") @Positive Long id) {
        logger.info("API request received: GET /api/departments/{}", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable("id") @Positive Long id, @Valid @RequestBody DepartmentRequestDto departmentRequest) {
        logger.info("API request received: PUT /api/departments/{}", id);
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("id") @Positive Long id) {
        logger.info("API request received: DELETE /api/departments/{}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
