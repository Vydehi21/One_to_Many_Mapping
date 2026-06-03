package com.swabhav.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDto {

	private long id;
	@JsonProperty("employee_name")
	private String employeeName;
	private String email;
	private double salary;
}
