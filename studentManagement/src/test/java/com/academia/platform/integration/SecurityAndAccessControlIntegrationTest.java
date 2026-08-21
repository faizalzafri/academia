package com.academia.platform.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class SecurityAndAccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUnauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void testStudentCannotAccessAdminApprovals() throws Exception {
        mockMvc.perform(get("/admin/approvals")
                .with(user("student1").authorities(() -> "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTeacherCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                .with(user("teacher1").authorities(() -> "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSystemAdminCanAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    public void testSystemAdminCanAccessApprovalsQueue() throws Exception {
        mockMvc.perform(get("/admin/approvals")
                .with(user("admin").authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().isOk());
    }
}
