package com.academia.platform.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.AcademicYearStatus;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.EnrollmentStatus;
import com.academia.platform.model.SchoolClass;
import com.academia.platform.model.Section;
import com.academia.platform.model.Student;
import com.academia.platform.model.StudentEnrollment;
import com.academia.platform.repository.AcademicYearRepository;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.SchoolClassRepository;
import com.academia.platform.repository.SectionRepository;
import com.academia.platform.repository.StudentEnrollmentRepository;
import com.academia.platform.repository.StudentRepository;
import com.academia.platform.service.AcademicService;
import com.academia.platform.service.AcademicYearService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AcademicYearIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private AcademicService academicService;

    @BeforeEach
    public void setUp() {
        academicService.seedInitialData();
    }

    @Test
    public void testCreateAndActivateAcademicYear() throws Exception {
        // 1. Principal creates new Academic Year in PLANNING status
        mockMvc.perform(post("/principal/years/create")
                .with(csrf())
                .with(user("admin").authorities(() -> "PRINCIPAL"))
                .param("name", "2027-2028")
                .param("startDate", "2027-04-01")
                .param("endDate", "2028-03-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/principal/years?created=true"));

        AcademicYear createdYear = academicYearRepository.findByName("2027-2028").orElseThrow();
        assertEquals(AcademicYearStatus.PLANNING, createdYear.getStatus());

        // 2. Activate the planned academic year
        mockMvc.perform(post("/principal/years/" + createdYear.getId() + "/activate")
                .with(csrf())
                .with(user("admin").authorities(() -> "PRINCIPAL")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/principal/years?activated=true"));

        AcademicYear activatedYear = academicYearRepository.findById(createdYear.getId()).orElseThrow();
        assertEquals(AcademicYearStatus.ACTIVE, activatedYear.getStatus());
    }

    @Test
    public void testBulkStudentPromotionWorkflow() {
        AcademicYear year2026 = academicYearService.getActiveAcademicYear();
        AcademicYear year2027 = academicYearService.createAcademicYear("2027-2028", LocalDate.of(2027, 4, 1), LocalDate.of(2028, 3, 31), null);

        SchoolClass class9 = schoolClassRepository.findByClassName("Class 9").orElseGet(() -> schoolClassRepository.save(new SchoolClass("Class 9")));
        SchoolClass class10 = schoolClassRepository.findByClassName("Class 10").orElseGet(() -> schoolClassRepository.save(new SchoolClass("Class 10")));
        SchoolClass class12 = schoolClassRepository.findByClassName("Class 12").orElseGet(() -> schoolClassRepository.save(new SchoolClass("Class 12")));
        Section sectionA = sectionRepository.findBySectionName("A").orElseGet(() -> sectionRepository.save(new Section("A")));

        ClassSection cs9A = classSectionRepository.save(new ClassSection(class9, sectionA, year2026));
        ClassSection cs12A = classSectionRepository.save(new ClassSection(class12, sectionA, year2026));

        // Create student 1 in Class 9
        Student s1 = new Student();
        s1.setUsername("student_nine");
        s1.setEmail("s9@academia.edu");
        s1.setPassword("pass");
        s1.setEnabled(true);
        studentRepository.save(s1);
        studentEnrollmentRepository.save(new StudentEnrollment(s1, cs9A, year2026, "R09-1", EnrollmentStatus.ENROLLED));

        // Create student 2 in Class 12 (graduating class)
        Student s2 = new Student();
        s2.setUsername("student_twelve");
        s2.setEmail("s12@academia.edu");
        s2.setPassword("pass");
        s2.setEnabled(true);
        studentRepository.save(s2);
        studentEnrollmentRepository.save(new StudentEnrollment(s2, cs12A, year2026, "R12-1", EnrollmentStatus.ENROLLED));

        // Execute bulk promotion from 2026 to 2027
        int promotedCount = academicYearService.promoteStudents(year2026.getId(), year2027.getId());
        assertEquals(1, promotedCount, "One student should be promoted to Class 10");

        // Verify Student 1 is promoted to Class 10 in 2027
        List<StudentEnrollment> s1Enrollments2027 = studentEnrollmentRepository.findByAcademicYear(year2027);
        assertEquals(1, s1Enrollments2027.size());
        assertEquals("Class 10", s1Enrollments2027.get(0).getClassSection().getSchoolClass().getClassName());
        assertEquals(EnrollmentStatus.ENROLLED, s1Enrollments2027.get(0).getStatus());

        // Verify Student 2 in 2026 is marked GRADUATED
        StudentEnrollment s2Enrollment = studentEnrollmentRepository.findByStudentAndAcademicYear(s2, year2026).orElseThrow();
        assertEquals(EnrollmentStatus.GRADUATED, s2Enrollment.getStatus());
    }
}
