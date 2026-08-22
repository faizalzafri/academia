package com.academia.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.AcademicYearStatus;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByStatus(AcademicYearStatus status);
    Optional<AcademicYear> findByName(String name);
    List<AcademicYear> findAllByOrderByStartDateDesc();
}
