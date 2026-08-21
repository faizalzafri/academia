package com.academia.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityCategory;
import com.academia.platform.model.ActivityStatus;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByAcademicYearOrderByEventDateAsc(AcademicYear academicYear);
    List<Activity> findByAcademicYearAndCategory(AcademicYear academicYear, ActivityCategory category);
    List<Activity> findByAcademicYearAndStatus(AcademicYear academicYear, ActivityStatus status);
    List<Activity> findAllByOrderByEventDateDesc();
    long countByAcademicYear(AcademicYear academicYear);
}
