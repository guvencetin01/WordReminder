package com.wordreminder.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Uses the current user's Windows Startup folder; no administrator rights are needed. */
public final class WindowsStartupManager {
    private static final String STARTUP_FILE = "WordReminder.cmd";

    public boolean isSupported() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public boolean isEnabled() {
        if (!isSupported()) return false;
        return Files.exists(getStartupFile());
    }

    public void setEnabled(boolean enabled) throws IOException {
        if (!isSupported()) {
            throw new UnsupportedOperationException("Otomatik başlatma yalnızca Windows üzerinde kullanılabilir.");
        }
        Path file = getStartupFile();
        if (!enabled) {
            Files.deleteIfExists(file);
            return;
        }

        Path startupDirectory = file.getParent();
        Files.createDirectories(startupDirectory);
        String command = buildLaunchCommand();
        String script = "@echo off\r\n" + command + " --background\r\n";
        Files.writeString(file, script, StandardCharsets.UTF_8);
    }

    private Path getStartupFile() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            throw new IllegalStateException("APPDATA ortam değişkeni bulunamadı.");
        }
        return Path.of(appData, "Microsoft", "Windows", "Start Menu", "Programs", "Startup", STARTUP_FILE);
    }

    private String buildLaunchCommand() {
        String packagedPath = System.getProperty("jpackage.app-path");
        if (packagedPath != null && !packagedPath.isBlank()) {
            return "start \"\" " + quote(packagedPath);
        }

        String classPath = System.getProperty("java.class.path", "");
        String modulePath = System.getProperty("jdk.module.path", "");
        String mainClass = "com.wordreminder.MainApplication";
        Path javaw = Path.of(System.getProperty("java.home"), "bin", "javaw.exe");
        StringBuilder command = new StringBuilder("start \"\" ").append(quote(javaw.toString()));
        if (!modulePath.isBlank()) {
            command.append(" --module-path ").append(quote(modulePath))
                    .append(" --add-modules javafx.controls");
        }
        command.append(" -cp ").append(quote(classPath)).append(" ").append(mainClass);
        return command.toString();
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
