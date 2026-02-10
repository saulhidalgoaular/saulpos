package com.saulpos.client.state;

import com.saulpos.api.auth.AuthTokenResponse;
import com.saulpos.client.api.ApiProblemException;
import com.saulpos.client.api.PosApiClient;
import com.saulpos.client.app.NavigationState;
import com.saulpos.client.app.NavigationTarget;
import com.saulpos.client.app.ScreenRegistry;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Clock;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class AuthSessionCoordinator {

    private static final long REFRESH_SAFETY_WINDOW_SECONDS = 30;

    private final PosApiClient apiClient;
    private final AppStateStore appStateStore;
    private final NavigationState navigationState;
    private final Clock clock;
    private final Consumer<Runnable> uiDispatcher;
    private final StringProperty sessionMessage = new SimpleStringProperty("Please sign in.");
    private final BooleanProperty authenticating = new SimpleBooleanProperty(false);
    private final AtomicBoolean refreshInFlight = new AtomicBoolean(false);

    public AuthSessionCoordinator(PosApiClient apiClient,
                                  AppStateStore appStateStore,
                                  NavigationState navigationState) {
        this(apiClient, appStateStore, navigationState, Clock.systemUTC(), Platform::runLater);
    }

    AuthSessionCoordinator(PosApiClient apiClient,
                           AppStateStore appStateStore,
                           NavigationState navigationState,
                           Clock clock,
                           Consumer<Runnable> uiDispatcher) {
        this.apiClient = apiClient;
        this.appStateStore = appStateStore;
        this.navigationState = navigationState;
        this.clock = clock;
        this.uiDispatcher = uiDispatcher;
    }

    public StringProperty sessionMessageProperty() {
        return sessionMessage;
    }

    public BooleanProperty authenticatingProperty() {
        return authenticating;
    }

    public CompletableFuture<Void> login(String username, String password) {
        dispatch(() -> authenticating.set(true));
        return apiClient.login(username, password)
                .thenCompose(token -> apiClient.currentUser().thenAccept(user -> dispatch(() -> {
                    appStateStore.updateSession(new AuthSessionState(
                            user.username(),
                            token.accessToken(),
                            token.refreshToken(),
                            user.roles(),
                            token.accessTokenExpiresAt(),
                            token.refreshTokenExpiresAt()
                    ));
                    sessionMessage.set("Session active for " + user.username() + ".");
                    navigationState.navigate(NavigationTarget.SHIFT_CONTROL);
                })))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        dispatch(() -> sessionMessage.set(mapLoginFailureMessage(throwable)));
                    }
                    dispatch(() -> authenticating.set(false));
                });
    }

    public CompletableFuture<Void> logout() {
        return apiClient.logout()
                .exceptionally(throwable -> null)
                .thenRun(() -> dispatch(() -> {
                    appStateStore.clearSession();
                    sessionMessage.set("Signed out.");
                    navigationState.navigate(NavigationTarget.LOGIN);
                }));
    }

    public void onNavigationChanged(NavigationTarget target) {
        boolean protectedScreen = ScreenRegistry.byTarget(target)
                .map(screen -> screen.requiresAuthenticatedSession())
                .orElse(false);
        if (!protectedScreen) {
            return;
        }

        AuthSessionState session = appStateStore.sessionState();
        if (session == null || !session.isAuthenticated()) {
            redirectToLogin("Authentication is required for this screen.");
            return;
        }

        if (shouldRefresh(session)) {
            refreshSession();
        }
    }

    public void refreshSession() {
        AuthSessionState session = appStateStore.sessionState();
        if (session == null || session.refreshToken() == null || session.refreshToken().isBlank()) {
            redirectToLogin("Session expired. Please sign in again.");
            return;
        }

        if (!refreshInFlight.compareAndSet(false, true)) {
            return;
        }

        apiClient.refresh(session.refreshToken())
                .thenAccept(token -> dispatch(() -> {
                    Set<String> permissions = token.roles() == null ? session.permissions() : token.roles();
                    appStateStore.updateSession(new AuthSessionState(
                            session.username(),
                            token.accessToken(),
                            token.refreshToken(),
                            permissions,
                            token.accessTokenExpiresAt(),
                            token.refreshTokenExpiresAt()
                    ));
                    sessionMessage.set("Session refreshed.");
                }))
                .exceptionally(throwable -> {
                    redirectToLogin(mapRefreshFailureMessage(throwable));
                    return null;
                })
                .whenComplete((ignored, throwable) -> refreshInFlight.set(false));
    }

    private boolean shouldRefresh(AuthSessionState session) {
        if (session.accessTokenExpiresAt() == null) {
            return false;
        }
        return !session.accessTokenExpiresAt().isAfter(clock.instant().plusSeconds(REFRESH_SAFETY_WINDOW_SECONDS));
    }

    private void redirectToLogin(String message) {
        dispatch(() -> {
            appStateStore.clearSession();
            sessionMessage.set(message);
            if (navigationState.activeTarget() != NavigationTarget.LOGIN) {
                navigationState.navigate(NavigationTarget.LOGIN);
            }
        });
    }

    private String mapLoginFailureMessage(Throwable throwable) {
        Throwable root = unwrap(throwable);
        if (root instanceof ApiProblemException problem) {
            if ("POS-4011".equals(problem.code())) {
                return "Invalid username or password.";
            }
            if ("POS-4012".equals(problem.code())) {
                return "Account is temporarily locked.";
            }
            if ("POS-4013".equals(problem.code())) {
                return "Account is disabled.";
            }
            return root.getMessage();
        }
        return "Unable to sign in. Verify server connectivity and try again.";
    }

    private String mapRefreshFailureMessage(Throwable throwable) {
        Throwable root = unwrap(throwable);
        if (root instanceof ApiProblemException problem) {
            if ("POS-4015".equals(problem.code()) || "POS-4014".equals(problem.code())) {
                return "Session expired. Please sign in again.";
            }
            return root.getMessage();
        }
        return "Session refresh failed. Please sign in again.";
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }

    private void dispatch(Runnable runnable) {
        uiDispatcher.accept(runnable);
    }
}
