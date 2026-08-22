package com.academia.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>{
	Optional<Course> findById(long id);
	}
