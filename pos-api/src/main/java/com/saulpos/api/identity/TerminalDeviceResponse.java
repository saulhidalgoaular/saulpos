package com.saulpos.api.identity;

public record TerminalDeviceResponse(
        Long id,
        Long storeLocationId,
        String code,
        String name,
        boolean active
) {
}
