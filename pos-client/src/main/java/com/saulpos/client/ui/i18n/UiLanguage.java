package com.saulpos.client.ui.i18n;

import java.util.Locale;

public enum UiLanguage {
    ENGLISH("English", Locale.ENGLISH),
    SPANISH("Espanol", Locale.forLanguageTag("es"));

    private final String label;
    private final Locale locale;

    UiLanguage(String label, Locale locale) {
        this.label = label;
        this.locale = locale;
    }

    public Locale locale() {
        return locale;
    }

    @Override
    public String toString() {
        return label;
    }
}
