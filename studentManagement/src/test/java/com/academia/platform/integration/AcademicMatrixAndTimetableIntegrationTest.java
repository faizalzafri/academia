package com.academia.platform.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.ClassSection;
import com.academia.platform.model.SchoolCalendarEvent;
import com.academia.platform.model.Subject;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.TimetableSlot;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.SchoolCalendarEventRepository;
import com.academia.platform.repository.SubjectRepository;
import com.academia.platform.repository.TeacherRepository;
import com.academia.platform.service.AcademicService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AcademicMatrixAndTimetableIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AcademicService academicService;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolCalendarEventRepository calendarEventRepository;

    private Teacher teacher;
    private Subject subject;
    private ClassSection classSection;

    @BeforeEach
    public void setUp() {
        academicService.seedInitialData();

        teacher = new Teacher("prof_test", "prof@test.com", "password", "Prof Test", "EMP999", "Professor", "Math");
        teacherRepository.save(teacher);

        List<Subject> subjects = subjectRepository.findAll();
        subject = subjects.get(0);

        List<ClassSection> classSections = classSectionRepository.findAll();
        classSection = classSections.get(0);
    }

    @Test
    public void testShowAcademicDashboard() throws Exception {
        mockMvc.perform(get("/academic/dashboard")
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("academic_dashboard"))
                .andExpect(model().attributeExists("classSections"))
                .andExpect(model().attributeExists("teachers"))
                .andExpect(model().attributeExists("subjects"));
    }

    @Test
    public void testAssignClassTeacher() throws Exception {
        mockMvc.perform(post("/academic/assign")
                .with(csrf())
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN"))
                .param("classSectionId", classSection.getId().toString())
                .param("teacherUsername", teacher.getUsername())
                .param("captainUsername", "")
                .param("sportsCaptainUsername", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academic/dashboard?assignSuccess=true"));

        ClassSection updated = classSectionRepository.findById(classSection.getId()).orElseThrow();
        assertNotNull(updated.getClassTeacher());
        assertEquals("prof_test", updated.getClassTeacher().getUsername());
    }

    @Test
    public void testAddCalendarEvent() throws Exception {
        mockMvc.perform(post("/academic/calendar/add")
                .with(csrf())
                .with(user("faculty1").authorities(() -> "TEACHER"))
                .param("eventDate", "2026-10-15")
                .param("title", "Mid-Semester Examination")
                .param("description", "Evaluation of modules 1 through 3")
                .param("type", "EXAM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academic/calendar?success=true"));

        List<SchoolCalendarEvent> events = calendarEventRepository.findAll();
        assertTrue(events.stream().anyMatch(e -> "Mid-Semester Examination".equals(e.getTitle())));
    }
}
