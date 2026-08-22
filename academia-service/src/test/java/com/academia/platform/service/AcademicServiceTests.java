package com.academia.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.ClassSection;
import com.academia.platform.model.DayOfWeek;
import com.academia.platform.model.EventType;
import com.academia.platform.model.SchoolCalendarEvent;
import com.academia.platform.model.Subject;
import com.academia.platform.model.Teacher;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.SubjectRepository;
import com.academia.platform.repository.TeacherRepository;
import com.academia.platform.repository.UserRepository;

@SpringBootTest
@Transactional
public class AcademicServiceTests {

    @Autowired
    private AcademicService academicService;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    private Teacher testTeacher;
    private Teacher testTeacher2;
    private Subject testSubject;

    @BeforeEach
    public void setUp() {
        // Seed initial standard data if not run already
        academicService.seedInitialData();

        // Clear out any existing users under teacher1 / teacher2 usernames to avoid type/primary key conflicts
        if (userRepository.existsById("teacher1")) {
            userRepository.deleteById("teacher1");
        }
        if (userRepository.existsById("teacher2")) {
            userRepository.deleteById("teacher2");
        }
        userRepository.flush();

        testTeacher = new Teacher("teacher1", "t1@hanu.edu", "password", "Aman Verma", "EMP001", "PGT Math", "Math");
        teacherRepository.save(testTeacher);

        testTeacher2 = new Teacher("teacher2", "t2@hanu.edu", "password", "Priya Sharma", "EMP002", "TGT Science", "Science");
        teacherRepository.save(testTeacher2);

        testSubject = subjectRepository.findAll().get(0);
    }

    @Test
    public void testDataSeeding() {
        List<ClassSection> classSections = academicService.getAllClassSections();
        assertTrue(classSections.size() > 0, "ClassSections should be seeded");
        
        List<Subject> subjects = academicService.getAllSubjects();
        assertTrue(subjects.size() > 0, "Subjects should be seeded");
    }

    @Test
    public void testAddTimetableSlotSuccess() {
        ClassSection cs = classSectionRepository.findAll().get(0);
        
        String result = academicService.addTimetableSlot(
            cs.getId(), 
            testSubject.getId(), 
            testTeacher.getUsername(), 
            DayOfWeek.MONDAY, 
            1, 
            "08:30 AM", 
            "09:15 AM"
        );
        
        assertEquals("success", result);
    }

    @Test
    public void testTimetableTeacherConflict() {
        ClassSection cs1 = classSectionRepository.findAll().get(0);
        ClassSection cs2 = classSectionRepository.findAll().get(1);

        // Schedule teacher in classroom 1
        academicService.addTimetableSlot(
            cs1.getId(), 
            testSubject.getId(), 
            testTeacher.getUsername(), 
            DayOfWeek.MONDAY, 
            2, 
            "09:15 AM", 
            "10:00 AM"
        );

        // Attempt scheduling the same teacher in classroom 2 during the same day and period
        String result = academicService.addTimetableSlot(
            cs2.getId(), 
            testSubject.getId(), 
            testTeacher.getUsername(), 
            DayOfWeek.MONDAY, 
            2, 
            "09:15 AM", 
            "10:00 AM"
        );

        assertTrue(result.contains("Conflict: Teacher"), "Should report teacher booking conflict");
    }

    @Test
    public void testTimetableClassroomConflict() {
        ClassSection cs = classSectionRepository.findAll().get(0);

        // Schedule teacher 1 in the classroom during period 3
        academicService.addTimetableSlot(
            cs.getId(), 
            testSubject.getId(), 
            testTeacher.getUsername(), 
            DayOfWeek.MONDAY, 
            3, 
            "10:00 AM", 
            "10:45 AM"
        );

        // Attempt scheduling teacher 2 in the same classroom during period 3
        String result = academicService.addTimetableSlot(
            cs.getId(), 
            testSubject.getId(), 
            testTeacher2.getUsername(), 
            DayOfWeek.MONDAY, 
            3, 
            "10:00 AM", 
            "10:45 AM"
        );

        assertTrue(result.contains("Conflict:"), "Should report classroom busy conflict");
    }

    @Test
    public void testCalendarEvents() {
        SchoolCalendarEvent holiday = new SchoolCalendarEvent(
            LocalDate.of(2026, 8, 15), 
            "Independence Day", 
            "National Holiday", 
            EventType.HOLIDAY
        );
        academicService.addCalendarEvent(holiday);

        List<SchoolCalendarEvent> events = academicService.getAllCalendarEvents();
        assertFalse(events.isEmpty());
        assertEquals("Independence Day", events.get(0).getTitle());
    }

    private void assertFalse(boolean condition) {
        assertTrue(!condition);
    }
}
