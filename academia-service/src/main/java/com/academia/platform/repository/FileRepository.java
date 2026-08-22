package com.academia.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.File;

@Repository
public interface FileRepository extends JpaRepository<File, Integer>{
	Optional<File> findById(int id);
}