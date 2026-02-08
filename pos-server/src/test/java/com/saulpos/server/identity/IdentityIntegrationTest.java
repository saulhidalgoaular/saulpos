package com.saulpos.server.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.RoleRepository;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = {"PERM_CONFIGURATION_MANAGE"})
class IdentityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM user_account");
    }

    @Test
    void merchantStoreTerminalFlowSupportsActivationAndFetch() throws Exception {
        Long merchantId = createMerchant("MER-001", "Main Merchant");
        Long storeId = createStore(merchantId, "STORE-001", "Store Centro");
        Long terminalId = createTerminal(storeId, "TERM-001", "Front Register");

        mockMvc.perform(post("/api/identity/terminals/{id}/deactivate", terminalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(terminalId))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/identity/terminals/{id}", terminalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeLocationId").value(storeId))
                .andExpect(jsonPath("$.code").value("TERM-001"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void duplicateMerchantCodeReturnsConflict() throws Exception {
        createMerchant("MER-DUP", "Merchant One");

        mockMvc.perform(post("/api/identity/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "mer-dup",
                                  "name": "Merchant Two"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    @Test
    void storeUserAssignmentAllowsMultiStoreMappingAndValidatesReferences() throws Exception {
        RoleEntity role = new RoleEntity();
        role.setCode("CASHIER");
        role.setDescription("Cashier role");
        RoleEntity savedRole = roleRepository.save(role);

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername("operator");
        user.setPasswordHash("test-hash");
        user.setActive(true);
        UserAccountEntity savedUser = userAccountRepository.save(user);

        Long merchantId = createMerchant("MER-ASSIGN", "Merchant Assign");
        Long storeOneId = createStore(merchantId, "STORE-A", "Store A");
        Long storeTwoId = createStore(merchantId, "STORE-B", "Store B");

        mockMvc.perform(post("/api/identity/store-user-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "storeLocationId": %d,
                                  "roleId": %d
                                }
                                """.formatted(savedUser.getId(), storeOneId, savedRole.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.storeLocationId").value(storeOneId))
                .andExpect(jsonPath("$.roleId").value(savedRole.getId()));

        mockMvc.perform(post("/api/identity/store-user-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "storeLocationId": %d,
                                  "roleId": %d
                                }
                                """.formatted(savedUser.getId(), storeTwoId, savedRole.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeLocationId").value(storeTwoId));

        mockMvc.perform(get("/api/identity/store-user-assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(post("/api/identity/store-user-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "storeLocationId": %d,
                                  "roleId": %d
                                }
                                """.formatted(savedUser.getId(), storeOneId, savedRole.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/identity/store-user-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "storeLocationId": %d,
                                  "roleId": 999999
                                }
                                """.formatted(savedUser.getId(), storeOneId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));
    }

    private Long createMerchant(String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/identity/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s",
                                  "name": "%s"
                                }
                                """.formatted(code, name)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createStore(Long merchantId, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/identity/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "%s",
                                  "name": "%s"
                                }
                                """.formatted(merchantId, code, name)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createTerminal(Long storeLocationId, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/identity/terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "code": "%s",
                                  "name": "%s"
                                }
                                """.formatted(storeLocationId, code, name)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
