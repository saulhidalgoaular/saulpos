package com.saulpos.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.model.PermissionEntity;
import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.RolePermissionEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.model.UserRoleEntity;
import com.saulpos.server.security.repository.PermissionRepository;
import com.saulpos.server.security.repository.RolePermissionRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionMatrixIntegrationTest {

    private static final String TEST_PASSWORD = "Pass123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM payment_transition");
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        Map<String, PermissionEntity> permissions = seedPermissions();
        RoleEntity configRole = createRole("CONFIG_ADMIN", Set.of(PermissionCodes.CONFIGURATION_MANAGE), permissions);
        RoleEntity salesRole = createRole("SALES_AGENT", Set.of(PermissionCodes.SALES_PROCESS), permissions);
        RoleEntity refundRole = createRole("REFUND_AGENT", Set.of(PermissionCodes.REFUND_PROCESS), permissions);
        RoleEntity inventoryRole = createRole("INVENTORY_AGENT", Set.of(PermissionCodes.INVENTORY_ADJUST), permissions);
        RoleEntity reportRole = createRole("REPORT_AGENT", Set.of(PermissionCodes.REPORT_VIEW), permissions);
        RoleEntity limitedRole = createRole("LIMITED", Set.of(), permissions);

        createUser("config-user", configRole);
        createUser("sales-user", salesRole);
        createUser("refund-user", refundRole);
        createUser("inventory-user", inventoryRole);
        createUser("report-user", reportRole);
        createUser("limited-user", limitedRole);
    }

    @Test
    void currentUserIntrospectionAndRoleManagementEndpointsWork() throws Exception {
        String configToken = login("config-user");
        String salesToken = login("sales-user");

        mockMvc.perform(get("/api/security/permissions/current")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("config-user"))
                .andExpect(jsonPath("$.roles", hasItem("CONFIG_ADMIN")))
                .andExpect(jsonPath("$.permissions", hasItem(PermissionCodes.CONFIGURATION_MANAGE)));

        mockMvc.perform(get("/api/security/permissions/catalog")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem(PermissionCodes.SALES_PROCESS)));

        MvcResult createRoleResult = mockMvc.perform(post("/api/security/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + configToken)
                        .content("""
                                {
                                  "code": "shift_lead",
                                  "description": "Shift Lead",
                                  "permissionCodes": ["report_view"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SHIFT_LEAD"))
                .andExpect(jsonPath("$.permissionCodes", hasItem(PermissionCodes.REPORT_VIEW)))
                .andReturn();

        JsonNode createdRole = objectMapper.readTree(createRoleResult.getResponse().getContentAsString());
        long roleId = createdRole.get("id").asLong();

        mockMvc.perform(put("/api/security/roles/{id}/permissions", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + configToken)
                        .content("""
                                {
                                  "permissionCodes": ["sales_process", "report_view"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", hasItem(PermissionCodes.SALES_PROCESS)))
                .andExpect(jsonPath("$.permissionCodes", hasItem(PermissionCodes.REPORT_VIEW)));

        mockMvc.perform(post("/api/security/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + salesToken)
                        .content("""
                                {
                                  "code": "blocked_role",
                                  "description": "blocked",
                                  "permissionCodes": ["sales_process"]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    void permissionMatrixEnforcesExplicitPermissionsByDomain() throws Exception {
        String configToken = login("config-user");
        String salesToken = login("sales-user");
        String refundToken = login("refund-user");
        String inventoryToken = login("inventory-user");
        String reportToken = login("report-user");
        String limitedToken = login("limited-user");

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "perm-checkout-limited")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "perm-checkout-sales")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/payments/1/capture")
                        .header("Idempotency-Key", "perm-capture-limited")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/payments/1/capture")
                        .header("Idempotency-Key", "perm-capture-sales")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/payments/1/refund")
                        .header("Idempotency-Key", "perm-refund-sales")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/payments/1/refund")
                        .header("Idempotency-Key", "perm-refund-user")
                        .header("Authorization", "Bearer " + refundToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(get("/api/payments/1")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/payments/1")
                        .header("Authorization", "Bearer " + refundToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/sales/carts/1/park")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/1/park")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/sales/carts/1/void")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "reasonCode": "SCAN_ERROR"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/1/void")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "reasonCode": "SCAN_ERROR"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/sales/carts/1/lines/1/price-override")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/1/lines/1/price-override")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/shifts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + limitedToken)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/shifts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + salesToken)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/refunds/submit")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/refunds/submit")
                        .header("Authorization", "Bearer " + refundToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/refunds/submit")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/inventory/adjustments")
                        .header("Authorization", "Bearer " + refundToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/inventory/adjustments")
                        .header("Authorization", "Bearer " + inventoryToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/inventory/adjustments")
                        .header("Authorization", "Bearer " + inventoryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/inventory/purchase-orders")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/inventory/purchase-orders")
                        .header("Authorization", "Bearer " + inventoryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/inventory/adjustments/1/approve")
                        .header("Authorization", "Bearer " + inventoryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/inventory/adjustments/1/approve")
                        .header("Authorization", "Bearer " + configToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(get("/api/inventory/balances")
                        .header("Authorization", "Bearer " + refundToken)
                        .param("storeLocationId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/inventory/balances")
                        .header("Authorization", "Bearer " + inventoryToken)
                        .param("storeLocationId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/inventory/movements")
                        .header("Authorization", "Bearer " + inventoryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "productId": 1,
                                  "movementType": "ADJUSTMENT",
                                  "quantityDelta": 1.000,
                                  "referenceType": "STOCK_ADJUSTMENT",
                                  "referenceNumber": "SEC-H1-1"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(get("/api/reports/sales")
                        .header("Authorization", "Bearer " + inventoryToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/reports/sales")
                        .header("Authorization", "Bearer " + reportToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/identity/merchants")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/identity/merchants")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/products")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/catalog/products")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .header("Authorization", "Bearer " + limitedToken)
                        .param("merchantId", "1")
                        .param("barcode", "1234567890"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .header("Authorization", "Bearer " + salesToken)
                        .param("merchantId", "1")
                        .param("barcode", "1234567890"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + limitedToken)
                        .param("merchantId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + salesToken)
                        .param("merchantId", "1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/customers/1/sales")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/customers/1/sales")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(get("/api/customers/1/returns")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/customers/1/returns")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + configToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .header("Authorization", "Bearer " + limitedToken)
                        .param("storeLocationId", "1")
                        .param("productId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .header("Authorization", "Bearer " + salesToken)
                        .param("storeLocationId", "1")
                        .param("productId", "1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/tax/preview")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": 1,
                                      "quantity": 1.0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/promotions/evaluate")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": 1,
                                      "quantity": 1.0,
                                      "unitPrice": 10.0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/promotions/evaluate")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": 1,
                                      "quantity": 1.0,
                                      "unitPrice": 10.0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/loyalty/earn")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "customerId": 1,
                                  "reference": "SEC-LOY-1",
                                  "saleGrossAmount": 10.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/loyalty/earn")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "customerId": 1,
                                  "reference": "SEC-LOY-2",
                                  "saleGrossAmount": 10.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/tax/preview")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": 1,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": 1,
                                      "quantity": 1.0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));
    }

    private Map<String, PermissionEntity> seedPermissions() {
        return Set.of(
                        PermissionCodes.SALES_PROCESS,
                        PermissionCodes.REFUND_PROCESS,
                        PermissionCodes.INVENTORY_ADJUST,
                        PermissionCodes.REPORT_VIEW,
                        PermissionCodes.CONFIGURATION_MANAGE)
                .stream()
                .map(code -> {
                    PermissionEntity permission = new PermissionEntity();
                    permission.setCode(code);
                    permission.setDescription(code + " permission");
                    return permissionRepository.save(permission);
                })
                .collect(java.util.stream.Collectors.toMap(PermissionEntity::getCode, permission -> permission));
    }

    private RoleEntity createRole(String code, Set<String> permissionCodes, Map<String, PermissionEntity> permissions) {
        RoleEntity role = new RoleEntity();
        role.setCode(code);
        role.setDescription(code + " role");
        RoleEntity savedRole = roleRepository.save(role);

        for (String permissionCode : permissionCodes) {
            RolePermissionEntity rolePermission = new RolePermissionEntity();
            rolePermission.setRole(savedRole);
            rolePermission.setPermission(permissions.get(permissionCode));
            rolePermissionRepository.save(rolePermission);
        }

        return savedRole;
    }

    private void createUser(String username, RoleEntity role) {
        UserAccountEntity user = new UserAccountEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setActive(true);
        UserAccountEntity savedUser = userAccountRepository.save(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
