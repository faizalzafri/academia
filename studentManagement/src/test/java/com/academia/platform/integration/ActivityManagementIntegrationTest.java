package com.academia.platform.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityCategory;
import com.academia.platform.model.ActivityParticipant;
import com.academia.platform.model.ActivityResult;
import com.academia.platform.model.Student;
import com.academia.platform.repository.ActivityParticipantRepository;
import com.academia.platform.repository.ActivityRepository;
import com.academia.platform.repository.ActivityResultRepository;
import com.academia.platform.repository.StudentRepository;
import com.academia.platform.service.ActivityService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class ActivityManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    private ActivityResultRepository activityResultRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ActivityService activityService;

    @Test
    public void testCreateActivityAndRegisterStudentWorkflow() throws Exception {
        // 1. Teacher/Principal creates an Extra-Curricular activity
        mockMvc.perform(post("/activities/create")
                .with(csrf())
                .with(user("faculty1").authorities(() -> "TEACHER"))
                .param("name", "Annual Sports Meet 2026")
                .param("description", "Track and field events and football tournament")
                .param("category", "SPORTS")
                .param("eventDate", "2026-10-20")
                .param("venue", "University Main Grounds"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities/manage?created=true"));

        List<Activity> activities = activityRepository.findAll();
        Activity sportsMeet = activities.stream()
                .filter(a -> "Annual Sports Meet 2026".equals(a.getName()))
                .findFirst().orElseThrow();

        assertEquals(ActivityCategory.SPORTS, sportsMeet.getCategory());

        // 2. Student registers for the activity
        Student student = new Student();
        student.setUsername("athlete_sam");
        student.setEmail("sam@academia.edu");
        student.setPassword("pass");
        student.setEnabled(true);
        studentRepository.save(student);

        mockMvc.perform(post("/activities/" + sportsMeet.getId() + "/register")
                .with(csrf())
                .with(user("athlete_sam").authorities(() -> "USER"))
                .param("studentUsername", "athlete_sam")
                .param("role", "Participant")
                .param("teamName", "Red House"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities/" + sportsMeet.getId() + "?result=success"));

        List<ActivityParticipant> participants = activityParticipantRepository.findByActivity(sportsMeet);
        assertEquals(1, participants.size());
        assertEquals("athlete_sam", participants.get(0).getStudent().getUsername());
        assertEquals("Red House", participants.get(0).getTeamName());

        // 3. Coordinator records sub-event winner result
        mockMvc.perform(post("/activities/" + sportsMeet.getId() + "/result")
                .with(csrf())
                .with(user("faculty1").authorities(() -> "TEACHER"))
                .param("eventName", "100m Dash")
                .param("winnerName", "Sam Athlete")
                .param("winnerClass", "Class 11-A")
                .param("position", "Gold Medal")
                .param("score", "10.85s"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities/" + sportsMeet.getId() + "?resultSaved=true"));

        List<ActivityResult> results = activityResultRepository.findByActivity(sportsMeet);
        assertEquals(1, results.size());
        assertEquals("100m Dash", results.get(0).getEventName());
        assertEquals("Gold Medal", results.get(0).getPosition());

        // 4. View activity detail page
        mockMvc.perform(get("/activities/" + sportsMeet.getId())
                .with(user("athlete_sam").authorities(() -> "USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("activity_detail"))
                .andExpect(model().attributeExists("activity"))
                .andExpect(model().attributeExists("participants"))
                .andExpect(model().attributeExists("results"));
    }
}
