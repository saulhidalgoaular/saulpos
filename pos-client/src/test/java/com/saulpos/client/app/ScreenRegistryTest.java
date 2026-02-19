package com.saulpos.client.app;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenRegistryTest {

    @Test
    void screenMap_shouldContainExpectedNavigationFlow() {
        List<ScreenDefinition> screens = ScreenRegistry.orderedScreens();

        assertEquals(NavigationTarget.LOGIN, screens.get(0).target());
        assertEquals(NavigationTarget.SHIFT_CONTROL, screens.get(1).target());
        assertEquals(NavigationTarget.SELL, screens.get(2).target());
        assertEquals(9, screens.size());

        Set<NavigationTarget> uniqueTargets = new HashSet<>();
        for (ScreenDefinition screen : screens) {
            uniqueTargets.add(screen.target());
        }
        assertEquals(screens.size(), uniqueTargets.size());
    }

    @Test
    void protectedScreens_shouldRequireAuthenticatedSession() {
        assertTrue(ScreenRegistry.byTarget(NavigationTarget.CHECKOUT).orElseThrow().requiresAuthenticatedSession());
        assertTrue(ScreenRegistry.byTarget(NavigationTarget.REPORTING).orElseThrow().requiresAuthenticatedSession());
        assertTrue(ScreenRegistry.byTarget(NavigationTarget.ADMIN).orElseThrow().requiresAuthenticatedSession());
    }
}
