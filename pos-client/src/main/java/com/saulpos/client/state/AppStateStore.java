package com.saulpos.client.state;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class AppStateStore {

    private final ObjectProperty<AuthSessionState> sessionState = new SimpleObjectProperty<>();
    private final BooleanBinding authenticated = Bindings.createBooleanBinding(
            () -> sessionState.get() != null && sessionState.get().isAuthenticated(),
            sessionState
    );

    public ObjectProperty<AuthSessionState> sessionStateProperty() {
        return sessionState;
    }

    public AuthSessionState sessionState() {
        return sessionState.get();
    }

    public BooleanBinding authenticatedProperty() {
        return authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public void updateSession(AuthSessionState session) {
        sessionState.set(session);
    }

    public void clearSession() {
        sessionState.set(null);
    }
}
