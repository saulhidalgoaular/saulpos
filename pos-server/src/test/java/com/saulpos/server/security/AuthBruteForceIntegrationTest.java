package com.saulpos.server.security;

import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.model.UserRoleEntity;
import com.saulpos.server.security.repository.RoleRepository;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.security.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.max-failed-attempts=2",
        "app.security.lock-duration-minutes=30"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthBruteForceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        RoleEntity role = new RoleEntity();
        role.setCode("CASHIER");
        role.setDescription("Cashier role");
        RoleEntity savedRole = roleRepository.save(role);

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername("clerk");
        user.setPasswordHash(passwordEncoder.encode("Pass123!"));
        user.setActive(true);
        UserAccountEntity savedUser = userAccountRepository.save(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(savedUser);
        userRole.setRole(savedRole);
        userRoleRepository.save(userRole);
    }

    @Test
    void accountLocksAfterConfiguredFailedAttempts() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "clerk",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("POS-4011"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "clerk",
                                  "password": "still-wrong"
                                }
                                """))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("POS-4012"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "clerk",
                                  "password": "Pass123!"
                                }
                                """))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("POS-4012"));
    }
}
