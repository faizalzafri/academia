package com.academia.platform.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.platform.model.Student;
import com.academia.platform.model.User;

@Transactional
public interface StudentRepository extends JpaRepository<Student, String>{
	Optional<Student> findById(String id);
}
