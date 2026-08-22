package com.academia.platform.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
public class HistoricalDataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    public void testAuditHistoricalSessionRoster() throws Exception {
        // Create past year (2024-2025) marked as COMPLETED
        AcademicYear pastYear = academicYearService.createAcademicYear("2024-2025", LocalDate.of(2024, 4, 1), LocalDate.of(2025, 3, 31), null);
        pastYear.setStatus(AcademicYearStatus.COMPLETED);

        SchoolClass class10 = schoolClassRepository.findByClassName("Class 10").orElseGet(() -> schoolClassRepository.save(new SchoolClass("Class 10")));
        Section sectionA = sectionRepository.findBySectionName("A").orElseGet(() -> sectionRepository.save(new Section("A")));
        ClassSection pastClassSection = classSectionRepository.save(new ClassSection(class10, sectionA, pastYear));

        Student student = new Student();
        student.setUsername("alumni_john");
        student.setEmail("john@alumni.edu");
        student.setPassword("pass");
        student.setEnabled(true);
        studentRepository.save(student);

        studentEnrollmentRepository.save(new StudentEnrollment(student, pastClassSection, pastYear, "R-2024-01", EnrollmentStatus.PROMOTED));

        // Browse history view for past year
        mockMvc.perform(get("/principal/history")
                .with(user("admin").authorities(() -> "PRINCIPAL"))
                .param("yearId", pastYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("principal_history"))
                .andExpect(model().attributeExists("allYears"))
                .andExpect(model().attributeExists("selectedYear"))
                .andExpect(model().attributeExists("enrollments"));

        List<StudentEnrollment> pastEnrollments = studentEnrollmentRepository.findByAcademicYear(pastYear);
        assertEquals(1, pastEnrollments.size());
        assertEquals("alumni_john", pastEnrollments.get(0).getStudent().getUsername());
        assertEquals("Class 10 - A", pastEnrollments.get(0).getClassSection().getDisplayName());
    }
}
