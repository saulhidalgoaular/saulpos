package com.saulpos.server.uat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.authorization.PermissionCodes;
import com.saulpos.server.security.model.PermissionEntity;
import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.model.UserRoleEntity;
import com.saulpos.server.security.repository.PermissionRepository;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class P3SecurityComplianceIntegrationTest {

    private static final String AUTH_USERNAME = "p3-auditor";
    private static final String AUTH_PASSWORD = "Pass123!";

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
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
        List<String> tables = List.of(
                "idempotency_key_event",
                "gift_card_transaction",
                "gift_card",
                "store_credit_transaction",
                "store_credit_account",
                "receipt_print_event",
                "no_sale_drawer_event",
                "fiscal_event",
                "fiscal_document",
                "loyalty_event",
                "sale_return_refund",
                "sale_return_line",
                "sale_return",
                "payment_transition",
                "payment_allocation",
                "payment",
                "inventory_movement_lot",
                "inventory_lot_balance",
                "inventory_lot",
                "inventory_product_cost",
                "inventory_movement",
                "sale_line",
                "sale",
                "sale_override_event",
                "void_reason_code",
                "sale_cart_event",
                "parked_cart_reference",
                "sale_cart_line",
                "sale_cart",
                "goods_receipt",
                "purchase_order_line",
                "purchase_order",
                "supplier_return_line",
                "supplier_return",
                "stocktake_line",
                "stocktake_session",
                "stock_adjustment",
                "stock_transfer_line",
                "stock_transfer",
                "customer_group_assignment",
                "customer_group",
                "customer_contact",
                "customer_tax_identity",
                "customer",
                "promotion_window",
                "promotion_rule",
                "promotion",
                "discount_application",
                "discount_reason_code",
                "receipt_header",
                "receipt_sequence",
                "receipt_series",
                "rounding_policy",
                "store_tax_rule",
                "tax_group",
                "open_price_entry_audit",
                "store_price_override",
                "price_book_item",
                "price_book",
                "product_barcode",
                "product_variant",
                "product",
                "category",
                "cash_movement",
                "cash_shift",
                "supplier_contact",
                "supplier_terms",
                "supplier",
                "store_user_assignment",
                "terminal_device",
                "store_location",
                "merchant",
                "auth_audit_event",
                "auth_session",
                "user_role",
                "user_account");

        for (String table : tables) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }

        ensurePermission(PermissionCodes.REPORT_VIEW, "Allows report and analytics access");
        ensurePermission(PermissionCodes.CONFIGURATION_MANAGE, "Allows configuration and security changes");
        ensurePermission(PermissionCodes.CASH_DRAWER_OPEN, "Allows no-sale drawer open operations");

        RoleEntity savedRole = roleRepository.findByCode("MANAGER")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setCode("MANAGER");
                    role.setDescription("Manager");
                    return roleRepository.save(role);
                });

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername(AUTH_USERNAME);
        user.setPasswordHash(passwordEncoder.encode(AUTH_PASSWORD));
        user.setActive(true);
        UserAccountEntity savedUser = userAccountRepository.save(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(savedUser);
        userRole.setRole(savedRole);
        userRoleRepository.save(userRole);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-P3");
        merchant.setName("Merchant P3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-P3");
        store.setName("Store P3");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-P3");
        terminal.setName("Terminal P3");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();
    }

    @Test
    void rbacRegressionEnforcesAndAllowsSensitiveOperationsByPermission() throws Exception {
        mockMvc.perform(post("/api/security/roles")
                        .with(withAuthorityUser("p3-limited", "PERM_SALES_PROCESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "p3_blocked_role",
                                  "description": "blocked",
                                  "permissionCodes": ["report_view"]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/security/roles")
                        .with(withAuthorityUser("p3-admin", "PERM_CONFIGURATION_MANAGE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "p3_audit_role",
                                  "description": "P3 audit role",
                                  "permissionCodes": ["report_view"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("P3_AUDIT_ROLE"));

        mockMvc.perform(post("/api/receipts/drawer/open")
                        .with(withAuthorityUser("p3-limited", "PERM_SALES_PROCESS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(drawerOpenRequest("NO_SALE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/receipts/drawer/open")
                        .with(withAuthorityUser("p3-cashier", "PERM_CASH_DRAWER_OPEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "p3-rbac-corr-1")
                        .content(drawerOpenRequest("NO_SALE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/reports/exceptions")
                        .with(withAuthorityUser("p3-limited", "PERM_SALES_PROCESS")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/reports/exceptions")
                        .with(withAuthorityUser("p3-reporter", "PERM_REPORT_VIEW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    void authAndSensitiveActionAuditsAreCompleteAndQueryable() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(AUTH_USERNAME, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.path("accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/receipts/drawer/open")
                        .with(withAuthorityUser("p3-cashier", "PERM_CASH_DRAWER_OPEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "p3-audit-corr-1")
                        .content(drawerOpenRequest("NO_SALE_TEST")))
                .andExpect(status().isOk());

        Integer loginSuccessCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM auth_audit_event WHERE username = ? AND event_type = 'LOGIN' AND outcome = 'SUCCESS'",
                Integer.class,
                AUTH_USERNAME);
        Integer logoutSuccessCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM auth_audit_event WHERE username = ? AND event_type = 'LOGOUT' AND outcome = 'SUCCESS'",
                Integer.class,
                AUTH_USERNAME);
        Integer noSaleCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM no_sale_drawer_event WHERE correlation_id = 'p3-audit-corr-1'",
                Integer.class);

        assertThat(loginSuccessCount).isEqualTo(1);
        assertThat(logoutSuccessCount).isEqualTo(1);
        assertThat(noSaleCount).isEqualTo(1);

        mockMvc.perform(get("/api/reports/exceptions")
                        .with(withAuthorityUser("p3-reporter", "PERM_REPORT_VIEW"))
                        .param("eventType", "NO_SALE")
                        .param("reasonCode", "NO_SALE_TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].eventType").value("NO_SALE"))
                .andExpect(jsonPath("$.rows[0].reasonCode").value("NO_SALE_TEST"))
                .andExpect(jsonPath("$.rows[0].actorUsername").value("p3-cashier"))
                .andExpect(jsonPath("$.rows[0].correlationId").value("p3-audit-corr-1"));
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor withAuthorityUser(String username,
                                                                                             String authority) {
        return user(username).authorities(new SimpleGrantedAuthority(authority));
    }

    private String drawerOpenRequest(String reasonCode) {
        return """
                {
                  "terminalDeviceId": %d,
                  "reasonCode": "%s",
                  "note": "P3 compliance check",
                  "referenceNumber": "P3-NO-SALE-1"
                }
                """.formatted(terminalDeviceId, reasonCode);
    }

    private void ensurePermission(String code, String description) {
        permissionRepository.findByCode(code)
                .orElseGet(() -> {
                    PermissionEntity permission = new PermissionEntity();
                    permission.setCode(code);
                    permission.setDescription(description);
                    return permissionRepository.save(permission);
                });
    }
}
