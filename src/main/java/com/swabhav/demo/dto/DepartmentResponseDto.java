package com.swabhav.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDto {
	
	private long id;
	@JsonProperty("department_name")
	private String departmentName;
	private String location;
	private List<EmployeeResponseDto> employees;

}
