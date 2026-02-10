package com.saulpos.client.app;

import com.saulpos.client.state.AppStateStore;
import com.saulpos.client.state.AuthSessionState;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppStateStoreTest {

    @Test
    void sessionState_shouldReflectAuthenticationFlag() {
        AppStateStore store = new AppStateStore();
        assertFalse(store.isAuthenticated());

        store.updateSession(new AuthSessionState("cashier", "access-token", "refresh-token", Set.of("SALES_PROCESS")));
        assertTrue(store.isAuthenticated());

        store.clearSession();
        assertFalse(store.isAuthenticated());
    }
}
