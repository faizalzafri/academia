package com.academia.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.SchoolClass;
import com.academia.platform.model.Section;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    Optional<ClassSection> findBySchoolClassAndSection(SchoolClass schoolClass, Section section);

    @EntityGraph(attributePaths = {"schoolClass", "section", "classTeacher", "classCaptain", "sportsCaptain"})
    List<ClassSection> findByAcademicYear(AcademicYear academicYear);
}
