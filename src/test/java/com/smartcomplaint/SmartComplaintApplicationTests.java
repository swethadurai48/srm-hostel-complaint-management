package com.smartcomplaint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SmartComplaintApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that Spring application context boots up successfully
    }

    @Test
    void testNewAdminLoginSuccess() throws Exception {
        String loginPayload = """
            {
                "email": "r.haresh11.8.2007@gmail.com",
                "password": "SRM@484"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.email").value("r.haresh11.8.2007@gmail.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void testPreviousAdminCredentialsDisabled() throws Exception {
        String oldAdminPayload1 = """
            {
                "email": "swethadurai48@gmail.com",
                "password": "SRM@368"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oldAdminPayload1))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        String oldAdminPayload2 = """
            {
                "email": "admin@system.com",
                "password": "admin123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oldAdminPayload2))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testStudentAccountsUnchanged() throws Exception {
        String studentPayload = """
            {
                "email": "student@campus.edu",
                "password": "student123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("student@campus.edu"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }
}
