package com.academia.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.platform.model.Student;
import com.academia.platform.service.StudentService;

@RestController
@RequestMapping("api/v1/students")
public class StudentController {

	@Autowired
	private StudentService service;

	@GetMapping(path = "{studentId}")
	public Student getStudent(@PathVariable("studentId") String studentId) {
		return service.findById(studentId).orElse(new Student());
	}

}
