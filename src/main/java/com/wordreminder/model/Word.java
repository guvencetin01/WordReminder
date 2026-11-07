package com.wordreminder.model;

public record Word(
        long id,
        String english,
        String turkish,
        String example,
        String exampleTranslation,
        String pronunciation,
        String partOfSpeech,
        String source,
        CefrLevel level,
        WordCategory category,
        int knownCount,
        int repeatCount,
        boolean hardWord,
        boolean studied
) {
    public boolean isCustom() {
        return "CUSTOM".equalsIgnoreCase(source);
    }
}
