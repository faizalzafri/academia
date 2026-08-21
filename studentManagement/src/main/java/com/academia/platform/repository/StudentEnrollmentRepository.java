package com.academia.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.EnrollmentStatus;
import com.academia.platform.model.Student;
import com.academia.platform.model.StudentEnrollment;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {
    List<StudentEnrollment> findByAcademicYear(AcademicYear academicYear);
    List<StudentEnrollment> findByClassSection(ClassSection classSection);
    List<StudentEnrollment> findByClassSectionAndAcademicYear(ClassSection classSection, AcademicYear academicYear);
    List<StudentEnrollment> findByStudent(Student student);
    Optional<StudentEnrollment> findByStudentAndAcademicYear(Student student, AcademicYear academicYear);
    long countByAcademicYear(AcademicYear academicYear);
    long countByAcademicYearAndStatus(AcademicYear academicYear, EnrollmentStatus status);
}
