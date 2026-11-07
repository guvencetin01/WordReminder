package com.wordreminder;

import com.wordreminder.data.Database;
import com.wordreminder.model.Word;
import com.wordreminder.repository.SettingsRepository;
import com.wordreminder.repository.WordRepository;
import com.wordreminder.service.ReminderScheduler;
import com.wordreminder.service.AppLogger;
import com.wordreminder.service.WindowsStartupManager;
import com.wordreminder.ui.MainWindow;
import com.wordreminder.ui.ReminderPopup;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApplication extends Application {
    private WordRepository wordRepository;
    private SettingsRepository settingsRepository;
    private ReminderScheduler scheduler;
    private MainWindow mainWindow;
    private ReminderPopup activePopup;
    private TrayIcon trayIcon;
    private boolean exiting;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        try {
            Database database = new Database();
            database.initialize();
            wordRepository = new WordRepository(database);
            settingsRepository = new SettingsRepository(database);
            wordRepository.seedBuiltInWords();

            WindowsStartupManager startupManager = new WindowsStartupManager();
            scheduler = new ReminderScheduler(() -> {
                long queuedAt = System.nanoTime();
                try {
                    Platform.runLater(() -> {
                        long uiQueueDelayMillis = java.util.concurrent.TimeUnit.NANOSECONDS
                                .toMillis(System.nanoTime() - queuedAt);
                        if (uiQueueDelayMillis >= 2_000L) {
                            AppLogger.warn("JavaFX arayüz kuyruğu hatırlatmayı " + uiQueueDelayMillis
                                    + " ms bekletti; arayüz veya eşzamanlı veri işlemleri yoğun olabilir.");
                        }
                        showNextReminder();
                    });
                } catch (IllegalStateException ex) {
                    AppLogger.warn("JavaFX platformu kapandığı için zamanlanmış hatırlatma gönderilemedi.");
                }
            });
            mainWindow = new MainWindow(primaryStage, wordRepository, settingsRepository,
                    startupManager, scheduler::intervalChanged, scheduler::triggerNow);

            installSystemTray();
            mainWindow.setTrayAvailable(trayIcon != null);
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                if (trayIcon != null) {
                    mainWindow.hide();
                } else {
                    exitApplication();
                }
            });

            int interval = settingsRepository.getReminderMinutes();
            scheduler.start(interval);
            boolean backgroundStart = getParameters().getRaw().contains("--background");
            boolean startMinimized = settingsRepository.isStartMinimized();
            if (backgroundStart && startMinimized && trayIcon != null) {
                mainWindow.hide();
            } else {
                mainWindow.show();
            }
        } catch (IOException | SQLException ex) {
            AppLogger.error("Uygulama başlatılırken hata oluştu.", ex);
            showFatalError(ex);
        }
    }

    private void installSystemTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            PopupMenu menu = new PopupMenu();
            MenuItem openItem = new MenuItem("WordReminder'ı aç");
            openItem.addActionListener(event -> Platform.runLater(mainWindow::show));
            MenuItem remindItem = new MenuItem("Şimdi kelime göster");
            remindItem.addActionListener(event -> Platform.runLater(scheduler::triggerNow));
            MenuItem exitItem = new MenuItem("Çıkış");
            exitItem.addActionListener(event -> Platform.runLater(this::exitApplication));
            menu.add(openItem);
            menu.add(remindItem);
            menu.addSeparator();
            menu.add(exitItem);

            trayIcon = new TrayIcon(createTrayImage(), "WordReminder", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(event -> Platform.runLater(mainWindow::show));
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException | RuntimeException ex) {
            trayIcon = null;
            System.err.println("Sistem tepsisi başlatılamadı: " + ex.getMessage());
        }
    }

    private BufferedImage createTrayImage() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new java.awt.Color(48, 89, 224));
        graphics.fillRoundRect(1, 1, 30, 30, 9, 9);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        graphics.drawString("W", 6, 23);
        graphics.dispose();
        return image;
    }

    private void showNextReminder() {
        if (exiting) {
            return;
        }
        if (activePopup != null) {
            scheduler.retryAfterInterval();
            return;
        }
        try {
            Optional<Word> next = wordRepository.findNextForReview(
                    settingsRepository.getReminderLevel(), settingsRepository.getReminderCategory());
            if (next.isEmpty()) {
                scheduler.retryAfterInterval();
                return;
            }
            Word word = next.get();
            activePopup = new ReminderPopup(word, action -> handlePopupAction(word, action));
            activePopup.show();
            scheduler.reminderDisplayed();
        } catch (SQLException ex) {
            AppLogger.error("Hatırlatma için sıradaki kelime alınamadı.", ex);
            showError("Kelime getirilemedi", ex.getMessage());
            scheduler.retryAfterInterval();
        }
    }

    private void handlePopupAction(Word word, String action) {
        activePopup = null;
        boolean refreshRequired = false;
        try {
            switch (action) {
                case "KNOWN" -> {
                    wordRepository.recordReview(word.id(), "KNOWN");
                    refreshRequired = true;
                }
                case "REPEAT" -> {
                    wordRepository.recordReview(word.id(), "REPEAT");
                    scheduler.snooze(Duration.ofMinutes(10));
                    refreshRequired = true;
                }
                case "SNOOZE" -> scheduler.snooze(Duration.ofMinutes(5));
                case "HARD" -> {
                    wordRepository.setHardWord(word.id(), true);
                    refreshRequired = true;
                }
                case "UNHARD" -> {
                    wordRepository.setHardWord(word.id(), false);
                    refreshRequired = true;
                }
                case "DISMISS" -> { }
                default -> scheduler.retryAfterInterval();
            }
            // Dismiss and snooze do not change database content; avoid needlessly reloading
            // the 4,654-row table and statistics on the JavaFX application thread.
            if (refreshRequired) {
                mainWindow.refreshData();
            }
        } catch (SQLException ex) {
            AppLogger.error("Hatırlatma işlemi veritabanına kaydedilemedi.", ex);
            showError("Tekrar kaydedilemedi", ex.getMessage());
            scheduler.retryAfterInterval();
        }
    }

    private void exitApplication() {
        if (exiting) return;
        exiting = true;
        if (scheduler != null) scheduler.close();
        if (activePopup != null) activePopup.close();
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
        Platform.exit();
    }

    private void showFatalError(Exception exception) {
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("WordReminder başlatılamadı");
        alert.setHeaderText("Uygulama başlatılırken bir hata oluştu.");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
        Platform.exit();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Bilinmeyen hata" : message);
        alert.show();
    }

    @Override
    public void stop() {
        if (scheduler != null) scheduler.close();
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
