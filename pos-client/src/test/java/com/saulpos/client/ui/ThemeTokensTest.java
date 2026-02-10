package com.saulpos.client.ui;

import com.saulpos.client.ui.theme.ThemeTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeTokensTest {

    @Test
    void primaryTextContrast_shouldMeetAccessibilityTarget() {
        double contrast = ThemeTokens.contrastRatio(ThemeTokens.COLOR_TEXT_PRIMARY, ThemeTokens.COLOR_BG_SURFACE);
        assertTrue(contrast >= 7.0, "Primary text contrast must meet WCAG AAA for readability");
    }

    @Test
    void actionButtonContrast_shouldBeReadableAgainstBackground() {
        double contrast = ThemeTokens.contrastRatio("#FFFFFF", ThemeTokens.COLOR_ACTION_PRIMARY);
        assertTrue(contrast >= 4.5, "Action button contrast must meet WCAG AA");
    }
}
