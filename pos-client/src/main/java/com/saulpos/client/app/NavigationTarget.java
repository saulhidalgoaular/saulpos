package com.saulpos.client.app;

public enum NavigationTarget {
    LOGIN("/login"),
    SHIFT_CONTROL("/shift"),
    SELL("/sell"),
    CHECKOUT("/checkout"),
    RETURNS("/returns"),
    BACKOFFICE("/backoffice"),
    REPORTING("/reporting"),
    HARDWARE("/hardware");

    private final String route;

    NavigationTarget(String route) {
        this.route = route;
    }

    public String route() {
        return route;
    }
}
