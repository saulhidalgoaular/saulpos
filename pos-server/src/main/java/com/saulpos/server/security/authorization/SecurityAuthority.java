package com.saulpos.server.security.authorization;

import java.util.Locale;

public final class SecurityAuthority {

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String PERMISSION_PREFIX = "PERM_";

    private SecurityAuthority() {
    }

    public static String role(String roleCode) {
        return ROLE_PREFIX + normalize(roleCode);
    }

    public static String permission(String permissionCode) {
        return PERMISSION_PREFIX + normalize(permissionCode);
    }

    public static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
