package com.academia.platform.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class PrincipalAccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStudentCannotAccessPrincipalDashboard() throws Exception {
        mockMvc.perform(get("/principal/dashboard")
                .with(user("student1").authorities(() -> "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTeacherCannotAccessPrincipalDashboard() throws Exception {
        mockMvc.perform(get("/principal/dashboard")
                .with(user("teacher1").authorities(() -> "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPrincipalCanAccessPrincipalDashboard() throws Exception {
        mockMvc.perform(get("/principal/dashboard")
                .with(user("principal_user").authorities(() -> "PRINCIPAL")))
                .andExpect(status().isOk());
    }

    @Test
    public void testPrincipalCanAccessAcademicYears() throws Exception {
        mockMvc.perform(get("/principal/years")
                .with(user("principal_user").authorities(() -> "PRINCIPAL")))
                .andExpect(status().isOk());
    }

    @Test
    public void testPrincipalCanAccessHistoricalArchive() throws Exception {
        mockMvc.perform(get("/principal/history")
                .with(user("principal_user").authorities(() -> "PRINCIPAL")))
                .andExpect(status().isOk());
    }
}
