package com.saulpos.server.identity.service;

import com.saulpos.api.identity.MerchantRequest;
import com.saulpos.api.identity.MerchantResponse;
import com.saulpos.api.identity.StoreLocationRequest;
import com.saulpos.api.identity.StoreLocationResponse;
import com.saulpos.api.identity.StoreUserAssignmentRequest;
import com.saulpos.api.identity.StoreUserAssignmentResponse;
import com.saulpos.api.identity.TerminalDeviceRequest;
import com.saulpos.api.identity.TerminalDeviceResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.StoreUserAssignmentEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.StoreUserAssignmentRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.RoleRepository;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final MerchantRepository merchantRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final TerminalDeviceRepository terminalDeviceRepository;
    private final StoreUserAssignmentRepository storeUserAssignmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public MerchantResponse createMerchant(MerchantRequest request) {
        String code = normalizeCode(request.code());
        ensureMerchantCodeAvailable(code, null);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode(code);
        merchant.setName(normalizeName(request.name()));

        MerchantEntity saved = merchantRepository.save(merchant);
        return toMerchantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> listMerchants() {
        return merchantRepository.findAll(Sort.by("id"))
                .stream()
                .map(this::toMerchantResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(Long id) {
        return toMerchantResponse(requireMerchant(id));
    }

    @Transactional
    public MerchantResponse updateMerchant(Long id, MerchantRequest request) {
        MerchantEntity merchant = requireMerchant(id);
        String code = normalizeCode(request.code());
        ensureMerchantCodeAvailable(code, id);

        merchant.setCode(code);
        merchant.setName(normalizeName(request.name()));

        return toMerchantResponse(merchantRepository.save(merchant));
    }

    @Transactional
    public MerchantResponse setMerchantActive(Long id, boolean active) {
        MerchantEntity merchant = requireMerchant(id);
        merchant.setActive(active);
        return toMerchantResponse(merchantRepository.save(merchant));
    }

    @Transactional
    public StoreLocationResponse createStoreLocation(StoreLocationRequest request) {
        String code = normalizeCode(request.code());
        ensureStoreCodeAvailable(code, null);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(requireMerchant(request.merchantId()));
        store.setCode(code);
        store.setName(normalizeName(request.name()));

        return toStoreLocationResponse(storeLocationRepository.save(store));
    }

    @Transactional(readOnly = true)
    public List<StoreLocationResponse> listStoreLocations() {
        return storeLocationRepository.findAll(Sort.by("id"))
                .stream()
                .map(this::toStoreLocationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreLocationResponse getStoreLocation(Long id) {
        return toStoreLocationResponse(requireStoreLocation(id));
    }

    @Transactional
    public StoreLocationResponse updateStoreLocation(Long id, StoreLocationRequest request) {
        StoreLocationEntity store = requireStoreLocation(id);
        String code = normalizeCode(request.code());
        ensureStoreCodeAvailable(code, id);

        store.setMerchant(requireMerchant(request.merchantId()));
        store.setCode(code);
        store.setName(normalizeName(request.name()));

        return toStoreLocationResponse(storeLocationRepository.save(store));
    }

    @Transactional
    public StoreLocationResponse setStoreLocationActive(Long id, boolean active) {
        StoreLocationEntity storeLocation = requireStoreLocation(id);
        storeLocation.setActive(active);
        return toStoreLocationResponse(storeLocationRepository.save(storeLocation));
    }

    @Transactional
    public TerminalDeviceResponse createTerminalDevice(TerminalDeviceRequest request) {
        String code = normalizeCode(request.code());
        ensureTerminalCodeAvailable(code, null);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(requireStoreLocation(request.storeLocationId()));
        terminal.setCode(code);
        terminal.setName(normalizeName(request.name()));

        return toTerminalDeviceResponse(terminalDeviceRepository.save(terminal));
    }

    @Transactional(readOnly = true)
    public List<TerminalDeviceResponse> listTerminalDevices() {
        return terminalDeviceRepository.findAll(Sort.by("id"))
                .stream()
                .map(this::toTerminalDeviceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TerminalDeviceResponse getTerminalDevice(Long id) {
        return toTerminalDeviceResponse(requireTerminalDevice(id));
    }

    @Transactional
    public TerminalDeviceResponse updateTerminalDevice(Long id, TerminalDeviceRequest request) {
        TerminalDeviceEntity terminal = requireTerminalDevice(id);
        String code = normalizeCode(request.code());
        ensureTerminalCodeAvailable(code, id);

        terminal.setStoreLocation(requireStoreLocation(request.storeLocationId()));
        terminal.setCode(code);
        terminal.setName(normalizeName(request.name()));

        return toTerminalDeviceResponse(terminalDeviceRepository.save(terminal));
    }

    @Transactional
    public TerminalDeviceResponse setTerminalDeviceActive(Long id, boolean active) {
        TerminalDeviceEntity terminal = requireTerminalDevice(id);
        terminal.setActive(active);
        return toTerminalDeviceResponse(terminalDeviceRepository.save(terminal));
    }

    @Transactional
    public StoreUserAssignmentResponse createStoreUserAssignment(StoreUserAssignmentRequest request) {
        ensureAssignmentAvailable(request.userId(), request.storeLocationId(), request.roleId(), null);

        StoreUserAssignmentEntity assignment = new StoreUserAssignmentEntity();
        assignment.setUser(requireUser(request.userId()));
        assignment.setStoreLocation(requireStoreLocation(request.storeLocationId()));
        assignment.setRole(requireRole(request.roleId()));

        return toStoreUserAssignmentResponse(storeUserAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<StoreUserAssignmentResponse> listStoreUserAssignments() {
        return storeUserAssignmentRepository.findAll(Sort.by("id"))
                .stream()
                .map(this::toStoreUserAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreUserAssignmentResponse getStoreUserAssignment(Long id) {
        return toStoreUserAssignmentResponse(requireStoreUserAssignment(id));
    }

    @Transactional
    public StoreUserAssignmentResponse updateStoreUserAssignment(Long id, StoreUserAssignmentRequest request) {
        StoreUserAssignmentEntity assignment = requireStoreUserAssignment(id);
        ensureAssignmentAvailable(request.userId(), request.storeLocationId(), request.roleId(), id);

        assignment.setUser(requireUser(request.userId()));
        assignment.setStoreLocation(requireStoreLocation(request.storeLocationId()));
        assignment.setRole(requireRole(request.roleId()));

        return toStoreUserAssignmentResponse(storeUserAssignmentRepository.save(assignment));
    }

    @Transactional
    public StoreUserAssignmentResponse setStoreUserAssignmentActive(Long id, boolean active) {
        StoreUserAssignmentEntity assignment = requireStoreUserAssignment(id);
        assignment.setActive(active);
        return toStoreUserAssignmentResponse(storeUserAssignmentRepository.save(assignment));
    }

    private void ensureMerchantCodeAvailable(String code, Long currentMerchantId) {
        merchantRepository.findByCodeIgnoreCase(code)
                .ifPresent(existing -> {
                    if (currentMerchantId == null || !existing.getId().equals(currentMerchantId)) {
                        throw new BaseException(ErrorCode.CONFLICT, "merchant code already exists: " + code);
                    }
                });
    }

    private void ensureStoreCodeAvailable(String code, Long currentStoreId) {
        storeLocationRepository.findByCodeIgnoreCase(code)
                .ifPresent(existing -> {
                    if (currentStoreId == null || !existing.getId().equals(currentStoreId)) {
                        throw new BaseException(ErrorCode.CONFLICT, "store code already exists: " + code);
                    }
                });
    }

    private void ensureTerminalCodeAvailable(String code, Long currentTerminalId) {
        terminalDeviceRepository.findByCodeIgnoreCase(code)
                .ifPresent(existing -> {
                    if (currentTerminalId == null || !existing.getId().equals(currentTerminalId)) {
                        throw new BaseException(ErrorCode.CONFLICT, "terminal code already exists: " + code);
                    }
                });
    }

    private void ensureAssignmentAvailable(Long userId, Long storeLocationId, Long roleId, Long currentAssignmentId) {
        storeUserAssignmentRepository.findByUserIdAndStoreLocationIdAndRoleId(userId, storeLocationId, roleId)
                .ifPresent(existing -> {
                    if (currentAssignmentId == null || !existing.getId().equals(currentAssignmentId)) {
                        throw new BaseException(ErrorCode.CONFLICT,
                                "assignment already exists for userId=%d storeLocationId=%d roleId=%d"
                                        .formatted(userId, storeLocationId, roleId));
                    }
                });
    }

    private MerchantEntity requireMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "merchant not found: " + id));
    }

    private StoreLocationEntity requireStoreLocation(Long id) {
        return storeLocationRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "store location not found: " + id));
    }

    private TerminalDeviceEntity requireTerminalDevice(Long id) {
        return terminalDeviceRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "terminal device not found: " + id));
    }

    private StoreUserAssignmentEntity requireStoreUserAssignment(Long id) {
        return storeUserAssignmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "store user assignment not found: " + id));
    }

    private UserAccountEntity requireUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "user not found: " + id));
    }

    private RoleEntity requireRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "role not found: " + id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private MerchantResponse toMerchantResponse(MerchantEntity entity) {
        return new MerchantResponse(entity.getId(), entity.getCode(), entity.getName(), entity.isActive());
    }

    private StoreLocationResponse toStoreLocationResponse(StoreLocationEntity entity) {
        return new StoreLocationResponse(entity.getId(), entity.getMerchant().getId(), entity.getCode(), entity.getName(),
                entity.isActive());
    }

    private TerminalDeviceResponse toTerminalDeviceResponse(TerminalDeviceEntity entity) {
        return new TerminalDeviceResponse(entity.getId(), entity.getStoreLocation().getId(), entity.getCode(),
                entity.getName(), entity.isActive());
    }

    private StoreUserAssignmentResponse toStoreUserAssignmentResponse(StoreUserAssignmentEntity entity) {
        return new StoreUserAssignmentResponse(entity.getId(), entity.getUser().getId(), entity.getStoreLocation().getId(),
                entity.getRole().getId(), entity.isActive());
    }
}
