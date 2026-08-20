package edu.hanu.studentManagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hanu.studentManagement.model.ClassSection;
import edu.hanu.studentManagement.model.SchoolClass;
import edu.hanu.studentManagement.model.Section;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    Optional<ClassSection> findBySchoolClassAndSection(SchoolClass schoolClass, Section section);
}
