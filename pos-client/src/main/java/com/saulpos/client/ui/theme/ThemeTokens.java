package com.saulpos.client.ui.theme;

public final class ThemeTokens {

    public static final String FONT_FAMILY = "Poppins";
    public static final String COLOR_BG_APP = "#F7F3EA";
    public static final String COLOR_BG_SURFACE = "#FFFDF7";
    public static final String COLOR_TEXT_PRIMARY = "#1B1A17";
    public static final String COLOR_TEXT_MUTED = "#5E5B52";
    public static final String COLOR_ACTION_PRIMARY = "#004D40";
    public static final String COLOR_ACTION_ACCENT = "#C2410C";
    public static final String COLOR_BORDER = "#D8CCB8";
    public static final String COLOR_DANGER = "#B42318";

    private ThemeTokens() {
    }

    public static double contrastRatio(String foregroundHex, String backgroundHex) {
        double fg = relativeLuminance(parseChannel(foregroundHex, 1), parseChannel(foregroundHex, 3), parseChannel(foregroundHex, 5));
        double bg = relativeLuminance(parseChannel(backgroundHex, 1), parseChannel(backgroundHex, 3), parseChannel(backgroundHex, 5));
        double lighter = Math.max(fg, bg);
        double darker = Math.min(fg, bg);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(int r, int g, int b) {
        double rs = linearize(r / 255.0);
        double gs = linearize(g / 255.0);
        double bs = linearize(b / 255.0);
        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
    }

    private static double linearize(double value) {
        if (value <= 0.03928) {
            return value / 12.92;
        }
        return Math.pow((value + 0.055) / 1.055, 2.4);
    }

    private static int parseChannel(String colorHex, int startInclusive) {
        if (colorHex == null || colorHex.length() != 7 || colorHex.charAt(0) != '#') {
            throw new IllegalArgumentException("Color must be #RRGGBB format");
        }
        return Integer.parseInt(colorHex.substring(startInclusive, startInclusive + 2), 16);
    }
}
