package com.academia.platform.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.AcademicYearStatus;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.EnrollmentStatus;
import com.academia.platform.model.SchoolClass;
import com.academia.platform.model.Section;
import com.academia.platform.model.Student;
import com.academia.platform.model.StudentEnrollment;
import com.academia.platform.model.User;
import com.academia.platform.repository.AcademicYearRepository;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.SchoolClassRepository;
import com.academia.platform.repository.SectionRepository;
import com.academia.platform.repository.StudentEnrollmentRepository;
import com.academia.platform.repository.TeacherRepository;

@Service
@Transactional
public class AcademicYearService {

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    public AcademicYear getActiveAcademicYear() {
        return academicYearRepository.findByStatus(AcademicYearStatus.ACTIVE)
                .orElseGet(() -> {
                    List<AcademicYear> all = academicYearRepository.findAllByOrderByStartDateDesc();
                    if (!all.isEmpty()) {
                        return all.get(0);
                    }
                    AcademicYear defaultYear = new AcademicYear("2026-2027", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), AcademicYearStatus.ACTIVE);
                    return academicYearRepository.save(defaultYear);
                });
    }

    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAllByOrderByStartDateDesc();
    }

    public Optional<AcademicYear> getAcademicYearById(Long id) {
        return academicYearRepository.findById(id);
    }

    public AcademicYear createAcademicYear(String name, LocalDate startDate, LocalDate endDate, User creator) {
        AcademicYear year = new AcademicYear(name, startDate, endDate, AcademicYearStatus.PLANNING);
        year.setCreatedBy(creator);
        return academicYearRepository.save(year);
    }

    public AcademicYear activateAcademicYear(Long yearId) {
        // Deactivate currently active year if any
        Optional<AcademicYear> activeOpt = academicYearRepository.findByStatus(AcademicYearStatus.ACTIVE);
        if (activeOpt.isPresent()) {
            AcademicYear oldActive = activeOpt.get();
            oldActive.setStatus(AcademicYearStatus.COMPLETED);
            oldActive.setCompletedAt(LocalDateTime.now());
            academicYearRepository.save(oldActive);
        }

        AcademicYear newYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + yearId));
        newYear.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(newYear);
    }

    public AcademicYear completeAcademicYear(Long yearId) {
        AcademicYear year = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + yearId));
        year.setStatus(AcademicYearStatus.COMPLETED);
        year.setCompletedAt(LocalDateTime.now());
        return academicYearRepository.save(year);
    }

    public void cloneAcademicStructure(Long fromYearId, Long toYearId) {
        AcademicYear fromYear = academicYearRepository.findById(fromYearId).orElseThrow();
        AcademicYear toYear = academicYearRepository.findById(toYearId).orElseThrow();

        List<ClassSection> existingSections = classSectionRepository.findAll();
        for (ClassSection cs : existingSections) {
            if (fromYear.equals(cs.getAcademicYear())) {
                ClassSection newCs = new ClassSection(cs.getSchoolClass(), cs.getSection(), toYear);
                newCs.setClassTeacher(cs.getClassTeacher());
                classSectionRepository.save(newCs);
            }
        }
    }

    public int promoteStudents(Long fromYearId, Long toYearId) {
        AcademicYear fromYear = academicYearRepository.findById(fromYearId).orElseThrow();
        AcademicYear toYear = academicYearRepository.findById(toYearId).orElseThrow();

        List<StudentEnrollment> fromEnrollments = studentEnrollmentRepository.findByAcademicYear(fromYear);
        int promotedCount = 0;

        for (StudentEnrollment enrollment : fromEnrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                Student student = enrollment.getStudent();
                ClassSection currentSection = enrollment.getClassSection();
                SchoolClass currentClass = currentSection.getSchoolClass();
                Section section = currentSection.getSection();

                // Extract class grade number e.g. "Class 9" -> 9
                String className = currentClass.getClassName();
                int grade = extractGradeNumber(className);

                if (grade >= 12) {
                    // Class 12 students graduate
                    enrollment.setStatus(EnrollmentStatus.GRADUATED);
                    studentEnrollmentRepository.save(enrollment);
                } else {
                    int nextGrade = grade + 1;
                    String nextClassName = "Class " + nextGrade;
                    Optional<SchoolClass> nextClassOpt = schoolClassRepository.findByClassName(nextClassName);

                    if (nextClassOpt.isPresent()) {
                        SchoolClass nextClass = nextClassOpt.get();
                        // Find or create class section in target year
                        ClassSection nextSection = findOrCreateClassSection(nextClass, section, toYear);

                        // Mark old enrollment as PROMOTED
                        enrollment.setStatus(EnrollmentStatus.PROMOTED);
                        studentEnrollmentRepository.save(enrollment);

                        // Create new enrollment in target year
                        StudentEnrollment newEnrollment = new StudentEnrollment(student, nextSection, toYear, enrollment.getRollNumber(), EnrollmentStatus.ENROLLED);
                        studentEnrollmentRepository.save(newEnrollment);
                        promotedCount++;
                    }
                }
            }
        }
        return promotedCount;
    }

    private ClassSection findOrCreateClassSection(SchoolClass schoolClass, Section section, AcademicYear year) {
        for (ClassSection cs : classSectionRepository.findAll()) {
            if (schoolClass.equals(cs.getSchoolClass()) && section.equals(cs.getSection()) && year.equals(cs.getAcademicYear())) {
                return cs;
            }
        }
        ClassSection newSection = new ClassSection(schoolClass, section, year);
        return classSectionRepository.save(newSection);
    }

    private int extractGradeNumber(String className) {
        try {
            String num = className.replaceAll("[^0-9]", "");
            return num.isEmpty() ? 0 : Integer.parseInt(num);
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, Object> getYearSummary(Long yearId) {
        AcademicYear year = academicYearRepository.findById(yearId).orElseThrow();
        Map<String, Object> summary = new HashMap<>();

        long totalEnrollments = studentEnrollmentRepository.countByAcademicYear(year);
        long activeStudents = studentEnrollmentRepository.countByAcademicYearAndStatus(year, EnrollmentStatus.ENROLLED);
        long promotedStudents = studentEnrollmentRepository.countByAcademicYearAndStatus(year, EnrollmentStatus.PROMOTED);
        long graduatedStudents = studentEnrollmentRepository.countByAcademicYearAndStatus(year, EnrollmentStatus.GRADUATED);

        summary.put("year", year);
        summary.put("totalEnrollments", totalEnrollments);
        summary.put("activeStudents", activeStudents);
        summary.put("promotedStudents", promotedStudents);
        summary.put("graduatedStudents", graduatedStudents);
        summary.put("totalTeachers", teacherRepository.count());

        return summary;
    }
}
