package com.saulpos.server.shift;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
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
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class ShiftLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    private Long cashierUserId;
    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
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

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("cashier-b3");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-B3");
        merchant.setName("Merchant B3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-B3");
        store.setName("Store B3");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-B3");
        terminal.setName("Terminal B3");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();
    }

    @Test
    void shiftOpenMovementCloseFlowComputesExpectedAndVariance() throws Exception {
        Long shiftId = openShift(cashierUserId, terminalDeviceId, "100.00");

        mockMvc.perform(post("/api/shifts/{id}/cash-movements", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "movementType": "PAID_IN",
                                  "amount": 20.00,
                                  "note": "Safe top-up"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("PAID_IN"))
                .andExpect(jsonPath("$.amount").value(20.0));

        mockMvc.perform(post("/api/shifts/{id}/cash-movements", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "movementType": "PAID_OUT",
                                  "amount": 5.25,
                                  "note": "Petty cash"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("PAID_OUT"))
                .andExpect(jsonPath("$.amount").value(5.25));

        mockMvc.perform(post("/api/shifts/{id}/close", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countedCash": 112.75,
                                  "note": "End of day count"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.expectedCloseCash").value(114.75))
                .andExpect(jsonPath("$.countedCloseCash").value(112.75))
                .andExpect(jsonPath("$.varianceCash").value(-2.0));

        mockMvc.perform(get("/api/shifts/{id}", shiftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPaidIn").value(20.0))
                .andExpect(jsonPath("$.totalPaidOut").value(5.25))
                .andExpect(jsonPath("$.expectedCloseCash").value(114.75))
                .andExpect(jsonPath("$.countedCloseCash").value(112.75));
    }

    @Test
    void cannotOpenSecondShiftForSameCashierAndTerminal() throws Exception {
        openShift(cashierUserId, terminalDeviceId, "50.00");

        mockMvc.perform(post("/api/shifts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "openingCash": 25.00
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    @Test
    void invalidStateTransitionsAreRejected() throws Exception {
        Long shiftId = openShift(cashierUserId, terminalDeviceId, "40.00");

        mockMvc.perform(post("/api/shifts/{id}/close", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countedCash": 40.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/shifts/{id}/cash-movements", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "movementType": "PAID_IN",
                                  "amount": 5.00,
                                  "note": "late"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/shifts/{id}/close", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countedCash": 40.00
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        Long secondTerminalId = createActiveTerminal("TERM-B3-2");
        Long secondShiftId = openShift(cashierUserId, secondTerminalId, "60.00");

        mockMvc.perform(post("/api/shifts/{id}/cash-movements", secondShiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "movementType": "OPEN",
                                  "amount": 1.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private Long openShift(Long userId, Long terminalId, String openingCash) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/shifts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "openingCash": %s
                                }
                                """.formatted(userId, terminalId, openingCash)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createActiveTerminal(String code) {
        StoreLocationEntity store = storeLocationRepository.findAll().getFirst();
        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode(code);
        terminal.setName(code);
        terminal.setActive(true);
        return terminalDeviceRepository.save(terminal).getId();
    }
}
