package com.saulpos.client.app;

public record ScreenDefinition(
        NavigationTarget target,
        String title,
        boolean requiresAuthenticatedSession,
        int keyboardOrder,
        String description
) {
}
