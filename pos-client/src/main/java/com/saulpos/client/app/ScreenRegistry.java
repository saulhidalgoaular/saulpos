package com.saulpos.client.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScreenRegistry {

    private static final List<ScreenDefinition> ORDERED_SCREENS = List.of(
            new ScreenDefinition(NavigationTarget.LOGIN, "Sign In", false, 1,
                    "Authenticate cashier and load server-side auth context"),
            new ScreenDefinition(NavigationTarget.SHIFT_CONTROL, "Shift", true, 2,
                    "Open/close shift and perform paid-in/paid-out cash controls"),
            new ScreenDefinition(NavigationTarget.SELL, "Sell", true, 3,
                    "Main barcode/search/cart workstation"),
            new ScreenDefinition(NavigationTarget.CHECKOUT, "Checkout", true, 4,
                    "Tender allocation, rounding visibility, and sale commit"),
            new ScreenDefinition(NavigationTarget.RETURNS, "Returns", true, 5,
                    "Return/refund workflows with approval context"),
            new ScreenDefinition(NavigationTarget.BACKOFFICE, "Backoffice", true, 6,
                    "Catalog, pricing, customer, and supplier maintenance"),
            new ScreenDefinition(NavigationTarget.ADMIN, "Admin", true, 7,
                    "Identity and security administration (roles, permissions, and assignments)"),
            new ScreenDefinition(NavigationTarget.REPORTING, "Reporting", true, 8,
                    "Operational reports and CSV exports"),
            new ScreenDefinition(NavigationTarget.HARDWARE, "Hardware", true, 9,
                    "Receipt print/reprint and drawer controls")
    );

    private static final Map<NavigationTarget, ScreenDefinition> BY_TARGET =
            ORDERED_SCREENS.stream()
                    .collect(Collectors.toUnmodifiableMap(ScreenDefinition::target, Function.identity()));

    private ScreenRegistry() {
    }

    public static List<ScreenDefinition> orderedScreens() {
        return ORDERED_SCREENS;
    }

    public static Optional<ScreenDefinition> byTarget(NavigationTarget target) {
        return Optional.ofNullable(BY_TARGET.get(target));
    }
}
