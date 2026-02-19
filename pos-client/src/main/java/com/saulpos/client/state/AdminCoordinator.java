package com.saulpos.client.state;

import com.saulpos.api.identity.MerchantRequest;
import com.saulpos.api.identity.MerchantResponse;
import com.saulpos.api.identity.StoreLocationRequest;
import com.saulpos.api.identity.StoreLocationResponse;
import com.saulpos.api.identity.StoreUserAssignmentRequest;
import com.saulpos.api.identity.StoreUserAssignmentResponse;
import com.saulpos.api.identity.TerminalDeviceRequest;
import com.saulpos.api.identity.TerminalDeviceResponse;
import com.saulpos.api.security.PermissionResponse;
import com.saulpos.api.security.RolePermissionsUpdateRequest;
import com.saulpos.api.security.RoleRequest;
import com.saulpos.api.security.RoleResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class AdminCoordinator {

    private final PosApiClient apiClient;
    private final Consumer<Runnable> uiDispatcher;

    private final ObjectProperty<List<MerchantResponse>> merchants = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<StoreLocationResponse>> stores = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<TerminalDeviceResponse>> terminals = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<StoreUserAssignmentResponse>> assignments = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<RoleResponse>> roles = new SimpleObjectProperty<>(List.of());
    private final ObjectProperty<List<PermissionResponse>> permissions = new SimpleObjectProperty<>(List.of());
    private final StringProperty adminMessage = new SimpleStringProperty(
            "Administration ready: manage roles, permissions, stores, terminals, and assignments.");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    public AdminCoordinator(PosApiClient apiClient) {
        this(apiClient, Platform::runLater);
    }

    AdminCoordinator(PosApiClient apiClient, Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.uiDispatcher = uiDispatcher;
    }

    public ObjectProperty<List<MerchantResponse>> merchantsProperty() {
        return merchants;
    }

    public ObjectProperty<List<StoreLocationResponse>> storesProperty() {
        return stores;
    }

    public ObjectProperty<List<TerminalDeviceResponse>> terminalsProperty() {
        return terminals;
    }

    public ObjectProperty<List<StoreUserAssignmentResponse>> assignmentsProperty() {
        return assignments;
    }

    public ObjectProperty<List<RoleResponse>> rolesProperty() {
        return roles;
    }

    public ObjectProperty<List<PermissionResponse>> permissionsProperty() {
        return permissions;
    }

    public StringProperty adminMessageProperty() {
        return adminMessage;
    }

    public BooleanProperty busyProperty() {
        return busy;
    }

    public CompletableFuture<Void> refreshAll() {
        dispatch(() -> busy.set(true));
        CompletableFuture<List<MerchantResponse>> merchantsFuture = apiClient.listMerchants();
        CompletableFuture<List<StoreLocationResponse>> storesFuture = apiClient.listStoreLocations();
        CompletableFuture<List<TerminalDeviceResponse>> terminalsFuture = apiClient.listTerminalDevices();
        CompletableFuture<List<StoreUserAssignmentResponse>> assignmentsFuture = apiClient.listStoreUserAssignments();
        CompletableFuture<List<RoleResponse>> rolesFuture = apiClient.listRoles();
        CompletableFuture<List<PermissionResponse>> permissionsFuture = apiClient.permissionCatalog();

        return CompletableFuture.allOf(
                        merchantsFuture,
                        storesFuture,
                        terminalsFuture,
                        assignmentsFuture,
                        rolesFuture,
                        permissionsFuture
                )
                .thenRun(() -> dispatch(() -> {
                    merchants.set(merchantsFuture.join());
                    stores.set(storesFuture.join());
                    terminals.set(terminalsFuture.join());
                    assignments.set(assignmentsFuture.join());
                    roles.set(rolesFuture.join());
                    permissions.set(permissionsFuture.join());
                    adminMessage.set("Administration data refreshed.");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> refreshIdentity() {
        dispatch(() -> busy.set(true));
        CompletableFuture<List<MerchantResponse>> merchantsFuture = apiClient.listMerchants();
        CompletableFuture<List<StoreLocationResponse>> storesFuture = apiClient.listStoreLocations();
        CompletableFuture<List<TerminalDeviceResponse>> terminalsFuture = apiClient.listTerminalDevices();
        CompletableFuture<List<StoreUserAssignmentResponse>> assignmentsFuture = apiClient.listStoreUserAssignments();

        return CompletableFuture.allOf(merchantsFuture, storesFuture, terminalsFuture, assignmentsFuture)
                .thenRun(() -> dispatch(() -> {
                    merchants.set(merchantsFuture.join());
                    stores.set(storesFuture.join());
                    terminals.set(terminalsFuture.join());
                    assignments.set(assignmentsFuture.join());
                    adminMessage.set("Identity data refreshed.");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> refreshSecurity() {
        dispatch(() -> busy.set(true));
        CompletableFuture<List<RoleResponse>> rolesFuture = apiClient.listRoles();
        CompletableFuture<List<PermissionResponse>> permissionsFuture = apiClient.permissionCatalog();
        return CompletableFuture.allOf(rolesFuture, permissionsFuture)
                .thenRun(() -> dispatch(() -> {
                    roles.set(rolesFuture.join());
                    permissions.set(permissionsFuture.join());
                    adminMessage.set("Security catalog refreshed.");
                }))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveMerchant(Long merchantId, String code, String name) {
        if (isBlank(code) || isBlank(name)) {
            dispatch(() -> adminMessage.set("Merchant code and name are required."));
            return CompletableFuture.completedFuture(null);
        }
        MerchantRequest request = new MerchantRequest(normalizeCode(code), normalize(name));
        dispatch(() -> busy.set(true));
        CompletableFuture<MerchantResponse> call = merchantId == null
                ? apiClient.createMerchant(request)
                : apiClient.updateMerchant(merchantId, request);
        return call.thenCompose(saved -> apiClient.listMerchants()
                        .thenAccept(records -> dispatch(() -> {
                            merchants.set(records);
                            adminMessage.set((merchantId == null ? "Merchant created: " : "Merchant updated: ")
                                    + saved.code());
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> setMerchantActive(Long merchantId, boolean active) {
        if (merchantId == null) {
            dispatch(() -> adminMessage.set("Merchant ID is required."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        CompletableFuture<MerchantResponse> call = active
                ? apiClient.activateMerchant(merchantId)
                : apiClient.deactivateMerchant(merchantId);
        return call.thenCompose(saved -> apiClient.listMerchants()
                        .thenAccept(records -> dispatch(() -> {
                            merchants.set(records);
                            adminMessage.set("Merchant " + saved.code() + " is now " + (saved.active() ? "ACTIVE" : "INACTIVE") + ".");
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveStore(Long storeLocationId, Long merchantId, String code, String name) {
        if (merchantId == null || isBlank(code) || isBlank(name)) {
            dispatch(() -> adminMessage.set("Store merchant ID, code, and name are required."));
            return CompletableFuture.completedFuture(null);
        }
        StoreLocationRequest request = new StoreLocationRequest(merchantId, normalizeCode(code), normalize(name));
        dispatch(() -> busy.set(true));
        CompletableFuture<StoreLocationResponse> call = storeLocationId == null
                ? apiClient.createStoreLocation(request)
                : apiClient.updateStoreLocation(storeLocationId, request);
        return call.thenCompose(saved -> apiClient.listStoreLocations()
                        .thenAccept(records -> dispatch(() -> {
                            stores.set(records);
                            adminMessage.set((storeLocationId == null ? "Store created: " : "Store updated: ")
                                    + saved.code());
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> setStoreActive(Long storeLocationId, boolean active) {
        if (storeLocationId == null) {
            dispatch(() -> adminMessage.set("Store location ID is required."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        CompletableFuture<StoreLocationResponse> call = active
                ? apiClient.activateStoreLocation(storeLocationId)
                : apiClient.deactivateStoreLocation(storeLocationId);
        return call.thenCompose(saved -> apiClient.listStoreLocations()
                        .thenAccept(records -> dispatch(() -> {
                            stores.set(records);
                            adminMessage.set("Store " + saved.code() + " is now " + (saved.active() ? "ACTIVE" : "INACTIVE") + ".");
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveTerminal(Long terminalDeviceId, Long storeLocationId, String code, String name) {
        if (storeLocationId == null || isBlank(code) || isBlank(name)) {
            dispatch(() -> adminMessage.set("Terminal store location ID, code, and name are required."));
            return CompletableFuture.completedFuture(null);
        }
        TerminalDeviceRequest request = new TerminalDeviceRequest(storeLocationId, normalizeCode(code), normalize(name));
        dispatch(() -> busy.set(true));
        CompletableFuture<TerminalDeviceResponse> call = terminalDeviceId == null
                ? apiClient.createTerminalDevice(request)
                : apiClient.updateTerminalDevice(terminalDeviceId, request);
        return call.thenCompose(saved -> apiClient.listTerminalDevices()
                        .thenAccept(records -> dispatch(() -> {
                            terminals.set(records);
                            adminMessage.set((terminalDeviceId == null ? "Terminal created: " : "Terminal updated: ")
                                    + saved.code());
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> setTerminalActive(Long terminalDeviceId, boolean active) {
        if (terminalDeviceId == null) {
            dispatch(() -> adminMessage.set("Terminal device ID is required."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        CompletableFuture<TerminalDeviceResponse> call = active
                ? apiClient.activateTerminalDevice(terminalDeviceId)
                : apiClient.deactivateTerminalDevice(terminalDeviceId);
        return call.thenCompose(saved -> apiClient.listTerminalDevices()
                        .thenAccept(records -> dispatch(() -> {
                            terminals.set(records);
                            adminMessage.set("Terminal " + saved.code() + " is now " + (saved.active() ? "ACTIVE" : "INACTIVE") + ".");
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> saveStoreUserAssignment(Long assignmentId,
                                                           Long userId,
                                                           Long storeLocationId,
                                                           Long roleId) {
        if (userId == null || storeLocationId == null || roleId == null) {
            dispatch(() -> adminMessage.set("User ID, store location ID, and role ID are required for assignment."));
            return CompletableFuture.completedFuture(null);
        }
        StoreUserAssignmentRequest request = new StoreUserAssignmentRequest(userId, storeLocationId, roleId);
        dispatch(() -> busy.set(true));
        CompletableFuture<StoreUserAssignmentResponse> call = assignmentId == null
                ? apiClient.createStoreUserAssignment(request)
                : apiClient.updateStoreUserAssignment(assignmentId, request);
        return call.thenCompose(saved -> apiClient.listStoreUserAssignments()
                        .thenAccept(records -> dispatch(() -> {
                            assignments.set(records);
                            adminMessage.set((assignmentId == null ? "Assignment created: " : "Assignment updated: ")
                                    + "#" + saved.id());
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> setStoreUserAssignmentActive(Long assignmentId, boolean active) {
        if (assignmentId == null) {
            dispatch(() -> adminMessage.set("Assignment ID is required."));
            return CompletableFuture.completedFuture(null);
        }
        dispatch(() -> busy.set(true));
        CompletableFuture<StoreUserAssignmentResponse> call = active
                ? apiClient.activateStoreUserAssignment(assignmentId)
                : apiClient.deactivateStoreUserAssignment(assignmentId);
        return call.thenCompose(saved -> apiClient.listStoreUserAssignments()
                        .thenAccept(records -> dispatch(() -> {
                            assignments.set(records);
                            adminMessage.set("Assignment #" + saved.id() + " is now " + (saved.active() ? "ACTIVE" : "INACTIVE") + ".");
                        })))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> createRole(String code, String description, String permissionCodesCsv) {
        if (isBlank(code)) {
            dispatch(() -> adminMessage.set("Role code is required."));
            return CompletableFuture.completedFuture(null);
        }
        RoleRequest request = new RoleRequest(normalizeCode(code), normalizeOptional(description), parseCodes(permissionCodesCsv));
        dispatch(() -> busy.set(true));
        return apiClient.createRole(request)
                .thenCompose(saved -> refreshSecurity()
                        .thenRun(() -> dispatch(() -> adminMessage.set("Role created: " + saved.code() + "."))))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    public CompletableFuture<Void> updateRolePermissions(Long roleId, String permissionCodesCsv) {
        if (roleId == null) {
            dispatch(() -> adminMessage.set("Role ID is required to update permissions."));
            return CompletableFuture.completedFuture(null);
        }
        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest(parseCodes(permissionCodesCsv));
        dispatch(() -> busy.set(true));
        return apiClient.updateRolePermissions(roleId, request)
                .thenCompose(saved -> refreshSecurity()
                        .thenRun(() -> dispatch(() -> adminMessage.set("Role permissions updated for " + saved.code() + "."))))
                .whenComplete((ignored, throwable) -> finish(throwable));
    }

    private void finish(Throwable throwable) {
        if (throwable != null) {
            dispatch(() -> adminMessage.set(mapError(throwable)));
        }
        dispatch(() -> busy.set(false));
    }

    private String mapError(Throwable throwable) {
        Throwable root = throwable;
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            root = completionException.getCause();
        }
        if (root instanceof ApiProblemException problem) {
            if (problem.getMessage() != null && !problem.getMessage().isBlank()) {
                return problem.getMessage();
            }
            if (problem.code() != null) {
                return "Request failed: " + problem.code();
            }
        }
        return "Administration operation failed. Verify inputs and permissions.";
    }

    private Set<String> parseCodes(String permissionCodesCsv) {
        if (isBlank(permissionCodesCsv)) {
            return Set.of();
        }
        Set<String> parsed = new LinkedHashSet<>();
        String[] tokens = permissionCodesCsv.split("[,\\s]+");
        for (String token : tokens) {
            if (!token.isBlank()) {
                parsed.add(normalizeCode(token));
            }
        }
        return parsed;
    }

    private static String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
