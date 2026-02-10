package com.saulpos.client.ui;

import com.saulpos.client.ui.theme.DesignSystemCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesignSystemCatalogTest {

    @Test
    void componentCatalog_shouldDefineExpectedReusableComponents() {
        var specs = DesignSystemCatalog.componentSpecs();

        assertEquals(5, specs.size());

        Set<String> styleClasses = new HashSet<>();
        specs.forEach(spec -> styleClasses.add(spec.rootStyleClass()));

        assertTrue(styleClasses.contains("pos-button"));
        assertTrue(styleClasses.contains("pos-input"));
        assertTrue(styleClasses.contains("pos-table"));
        assertTrue(styleClasses.contains("pos-dialog"));
        assertTrue(styleClasses.contains("pos-toast"));
    }

    @Test
    void focusOrder_shouldBeDeterministicForKeyboardWorkflow() {
        var specs = DesignSystemCatalog.componentSpecs();
        for (int index = 0; index < specs.size(); index++) {
            assertEquals(index + 1, specs.get(index).keyboardOrder());
        }
    }
}
