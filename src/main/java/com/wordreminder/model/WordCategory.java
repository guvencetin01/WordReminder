package com.wordreminder.model;

import java.util.Arrays;

public enum WordCategory {
    DAILY("Günlük İngilizce"),
    TECHNICAL("Yazılım / Teknik İngilizce");

    private final String displayName;

    WordCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static WordCategory fromDatabase(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(DAILY);
    }
}
