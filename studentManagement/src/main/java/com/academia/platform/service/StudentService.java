package com.academia.platform.service;

import java.util.Optional;

import com.academia.platform.model.Student;
import com.academia.platform.dto.StudentDTO;

public interface StudentService {
	Student save(StudentDTO registration);
	Optional<Student> findById(String id);
	Student saveOrUpdate(Student student);
}
