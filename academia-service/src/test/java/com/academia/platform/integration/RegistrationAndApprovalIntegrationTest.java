package com.academia.platform.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.ApprovalStatus;
import com.academia.platform.model.Gender;
import com.academia.platform.model.Student;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.User;
import com.academia.platform.repository.UserRepository;
import com.academia.platform.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class RegistrationAndApprovalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testStudentRegistrationRequiresApproval() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("role", "STUDENT")
                .param("username", "test_student")
                .param("email", "student@test.com")
                .param("password", "Password123")
                .param("confirmPassword", "Password123")
                .param("name", "Test Student")
                .param("department", "Computer Science")
                .param("major", "Software Engineering")
                .param("gender", "MALE")
                .param("cohort", "2026-2030")
                .param("description", "Enthusiastic CS major"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?pendingApproval=true"));

        Optional<User> savedUserOpt = userRepository.findById("test_student");
        assertTrue(savedUserOpt.isPresent());

        User savedUser = savedUserOpt.get();
        assertTrue(savedUser instanceof Student);
        assertEquals(ApprovalStatus.PENDING, savedUser.getApprovalStatus());
        assertFalse(savedUser.isEnabled(), "User must be disabled until approved by System Admin");
        assertTrue(savedUser.getAuthorities().contains("USER"));
    }

    @Test
    public void testFacultyRegistrationRequiresApproval() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("role", "TEACHER")
                .param("username", "prof_morgan")
                .param("email", "morgan@academia.edu")
                .param("password", "Password123")
                .param("confirmPassword", "Password123")
                .param("name", "Dr. Alex Morgan")
                .param("department", "Physics")
                .param("employeeId", "FAC-1001")
                .param("designation", "Associate Professor")
                .param("specialization", "Quantum Mechanics")
                .param("description", "Faculty researcher"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?pendingApproval=true"));

        Optional<User> savedUserOpt = userRepository.findById("prof_morgan");
        assertTrue(savedUserOpt.isPresent());

        User savedUser = savedUserOpt.get();
        assertTrue(savedUser instanceof Teacher);
        Teacher teacher = (Teacher) savedUser;
        assertEquals("FAC-1001", teacher.getEmployeeId());
        assertEquals("Associate Professor", teacher.getDesignation());
        assertEquals(ApprovalStatus.PENDING, teacher.getApprovalStatus());
        assertFalse(teacher.isEnabled(), "Teacher must be disabled until approved");
        assertTrue(teacher.getAuthorities().contains("TEACHER"));
    }

    @Test
    public void testAdminApproveUserWorkflow() throws Exception {
        // 1. Create a pending student
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("role", "STUDENT")
                .param("username", "alice_app")
                .param("email", "alice@academia.edu")
                .param("password", "Password123")
                .param("confirmPassword", "Password123")
                .param("name", "Alice Applicant")
                .param("department", "Mathematics"))
                .andExpect(status().is3xxRedirection());

        User pendingUser = userRepository.findById("alice_app").orElseThrow();
        assertEquals(ApprovalStatus.PENDING, pendingUser.getApprovalStatus());
        assertFalse(pendingUser.isEnabled());

        // 2. Admin logs in and approves user
        mockMvc.perform(post("/admin/approve/alice_app")
                .with(csrf())
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals"));

        // 3. Verify user is now approved and enabled
        User approvedUser = userRepository.findById("alice_app").orElseThrow();
        assertEquals(ApprovalStatus.APPROVED, approvedUser.getApprovalStatus());
        assertTrue(approvedUser.isEnabled());
    }

    @Test
    public void testAdminRejectUserWorkflow() throws Exception {
        // 1. Create a pending user
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("role", "STUDENT")
                .param("username", "bob_rej")
                .param("email", "bob@academia.edu")
                .param("password", "Password123")
                .param("confirmPassword", "Password123")
                .param("name", "Bob Applicant")
                .param("department", "Chemistry"))
                .andExpect(status().is3xxRedirection());

        // 2. Admin rejects user
        mockMvc.perform(post("/admin/reject/bob_rej")
                .with(csrf())
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals"));

        // 3. Verify user is rejected and disabled
        User rejectedUser = userRepository.findById("bob_rej").orElseThrow();
        assertEquals(ApprovalStatus.REJECTED, rejectedUser.getApprovalStatus());
        assertFalse(rejectedUser.isEnabled());
    }
}
