package com.saulpos.client.ui.theme;

import java.util.List;

public final class DesignSystemCatalog {

    public enum ComponentType {
        BUTTON,
        INPUT,
        TABLE,
        DIALOG,
        TOAST
    }

    public record ComponentSpec(
            ComponentType type,
            String rootStyleClass,
            boolean focusable,
            int keyboardOrder
    ) {
    }

    private static final List<ComponentSpec> COMPONENT_SPECS = List.of(
            new ComponentSpec(ComponentType.INPUT, "pos-input", true, 1),
            new ComponentSpec(ComponentType.BUTTON, "pos-button", true, 2),
            new ComponentSpec(ComponentType.TABLE, "pos-table", true, 3),
            new ComponentSpec(ComponentType.DIALOG, "pos-dialog", true, 4),
            new ComponentSpec(ComponentType.TOAST, "pos-toast", false, 5)
    );

    private DesignSystemCatalog() {
    }

    public static List<ComponentSpec> componentSpecs() {
        return COMPONENT_SPECS;
    }
}
