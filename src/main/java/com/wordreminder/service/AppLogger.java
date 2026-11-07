package com.wordreminder.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/** Small rotating diagnostic log stored beside the user's application database. */
public final class AppLogger {
    private static final long MAX_LOG_BYTES = 1_000_000L;
    private static final Path LOG_FILE = resolveLogFile();
    private static final Path OLD_LOG_FILE = LOG_FILE.resolveSibling("wordreminder.log.1");

    private AppLogger() {
    }

    public static synchronized void info(String message) {
        append("INFO", message, null);
    }

    public static synchronized void warn(String message) {
        append("WARN", message, null);
    }

    public static synchronized void error(String message, Throwable error) {
        append("ERROR", message, error);
    }

    private static Path resolveLogFile() {
        String appData = System.getenv("APPDATA");
        Path directory = appData != null && !appData.isBlank()
                ? Path.of(appData, "WordReminder")
                : Path.of(System.getProperty("user.home"), ".wordreminder");
        return directory.resolve("wordreminder.log");
    }

    private static void append(String level, String message, Throwable error) {
        try {
            Files.createDirectories(LOG_FILE.getParent());
            if (Files.exists(LOG_FILE) && Files.size(LOG_FILE) >= MAX_LOG_BYTES) {
                Files.move(LOG_FILE, OLD_LOG_FILE, StandardCopyOption.REPLACE_EXISTING);
            }

            StringBuilder entry = new StringBuilder()
                    .append(Instant.now())
                    .append(" [").append(level).append("] ")
                    .append(message == null ? "" : message.replace('\n', ' ').replace('\r', ' '))
                    .append(System.lineSeparator());
            if (error != null) {
                StringWriter stackTrace = new StringWriter();
                error.printStackTrace(new PrintWriter(stackTrace));
                entry.append(stackTrace).append(System.lineSeparator());
            }
            Files.writeString(LOG_FILE, entry, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException loggingError) {
            System.err.println("WordReminder günlük kaydı yazılamadı: " + loggingError.getMessage());
        }
    }
}
