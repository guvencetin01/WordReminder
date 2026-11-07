package com.wordreminder.model;

import java.util.Arrays;

/** CEFR learning level. Vocabulary assignments are pedagogical estimates, not an official word-by-word CEFR list. */
public enum CefrLevel {
    A1, A2, B1, B2, C1, C2;

    public static CefrLevel fromDatabase(String value) {
        return Arrays.stream(values())
                .filter(level -> level.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(A2);
    }
}
