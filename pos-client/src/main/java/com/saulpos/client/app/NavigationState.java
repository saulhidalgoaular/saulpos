package com.saulpos.client.app;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class NavigationState {

    private final ObjectProperty<NavigationTarget> activeTarget = new SimpleObjectProperty<>(NavigationTarget.LOGIN);

    public ObjectProperty<NavigationTarget> activeTargetProperty() {
        return activeTarget;
    }

    public NavigationTarget activeTarget() {
        return activeTarget.get();
    }

    public void navigate(NavigationTarget target) {
        ScreenRegistry.byTarget(target)
                .orElseThrow(() -> new IllegalArgumentException("Unknown navigation target: " + target));
        activeTarget.set(target);
    }
}
