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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.Course;
import com.academia.platform.repository.CourseRepository;
import com.academia.platform.service.CourseService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class CourseManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Test
    public void testCreateAndListCourse() throws Exception {
        // Teacher creates course
        mockMvc.perform(post("/addCourse")
                .with(csrf())
                .with(user("faculty1").authorities(() -> "TEACHER"))
                .param("code", "CS301")
                .param("name", "Database Systems")
                .param("credit", "4")
                .param("description", "Relational algebra and SQL storage engines"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manage/course?success"));

        List<Course> courses = courseRepository.findAll();
        assertTrue(courses.stream().anyMatch(c -> "CS301".equals(c.getCode())));

        // Student views course catalog
        mockMvc.perform(get("/listCourse")
                .with(user("student1").authorities(() -> "USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("list-course"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    public void testUpdateCourseSyllabus() throws Exception {
        Course course = new Course();
        course.setCode("MATH201");
        course.setName("Linear Algebra");
        course.setCredit(3);
        course.setDescription("Initial syllabus outline");
        course = courseRepository.save(course);

        mockMvc.perform(post("/updateCourse/" + course.getId())
                .with(csrf())
                .with(user("faculty1").authorities(() -> "TEACHER"))
                .param("description", "Updated advanced syllabus: Eigenvalues and SVD")
                .param("references", "Gilbert Strang Linear Algebra"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/course/" + course.getId() + "?success"));

        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertEquals("Updated advanced syllabus: Eigenvalues and SVD", updated.getDescription());
    }

    @Test
    public void testStudentSyllabusView() throws Exception {
        Course course = new Course();
        course.setCode("PHYS101");
        course.setName("Mechanics");
        course.setCredit(4);
        course.setDescription("Newtonian mechanics");
        course = courseRepository.save(course);

        mockMvc.perform(get("/student/course/" + course.getId())
                .with(user("student1").authorities(() -> "USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("student-course"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("files"));
    }
}
