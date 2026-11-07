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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
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
    private boolean reminderRequestInFlight;
    private volatile boolean reminderDispatchQueued;
    private boolean reminderPendingWhilePopupOpen;
    private boolean presentationRecordedForActivePopup;
    private boolean catalogReady;
    private ExecutorService databaseExecutor;
    private ExecutorService reminderExecutor;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        try {
            Database database = new Database();
            database.initialize();
            databaseExecutor = Executors.newFixedThreadPool(3, runnable -> {
                Thread thread = new Thread(runnable, "wordreminder-database");
                thread.setDaemon(true);
                return thread;
            });
            reminderExecutor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "wordreminder-reminder-db");
                thread.setDaemon(true);
                return thread;
            });
            wordRepository = new WordRepository(database);
            settingsRepository = new SettingsRepository(database);

            WindowsStartupManager startupManager = new WindowsStartupManager();
            scheduler = new ReminderScheduler(() -> {
                long queuedAt = System.nanoTime();
                try {
                    if (reminderDispatchQueued) return;
                    reminderDispatchQueued = true;
                    Platform.runLater(() -> {
                        reminderDispatchQueued = false;
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
                    startupManager, scheduler::intervalChanged, scheduler::triggerNow, databaseExecutor);

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

            boolean backgroundStart = getParameters().getRaw().contains("--background");
            boolean startMinimized = settingsRepository.isStartMinimized();
            boolean hideAtStartup = backgroundStart && startMinimized && trayIcon != null;
            if (!hideAtStartup) {
                mainWindow.show();
            }

            // The initial 4,654-row catalogue seed can touch thousands of SQLite rows.
            // Keep it off the JavaFX thread, then start the reminder clock from completion.
            databaseExecutor.execute(() -> {
                try {
                    wordRepository.seedBuiltInWords();
                    int reminderInterval = settingsRepository.getReminderMinutes();
                    postToFx(() -> {
                        if (exiting) return;
                        catalogReady = true;
                        scheduler.start(reminderInterval);
                        mainWindow.refreshData();
                        if (hideAtStartup) mainWindow.hide();
                    });
                } catch (IOException | SQLException ex) {
                    AppLogger.error("Kelime kataloğu hazırlanırken hata oluştu.", ex);
                    postToFx(() -> {
                        if (!exiting) showFatalError(ex);
                    });
                } catch (RejectedExecutionException ex) {
                    if (!exiting) AppLogger.error("Kelime kataloğu görevi kuyruğa alınamadı.", ex);
                }
            });
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

    /**
     * Runs the reminder lookup on a database worker. The JavaFX thread only creates and
     * displays the card after the query has completed.
     */
    private void showNextReminder() {
        if (exiting) return;
        if (!catalogReady) {
            scheduler.retryAfterInterval();
            return;
        }
        if (activePopup != null) {
            // The interval elapsed while a card was visible. Do not start another timer:
            // deliver one reminder as soon as this card is closed.
            reminderPendingWhilePopupOpen = true;
            return;
        }
        if (reminderRequestInFlight) {
            // A lookup is already in progress; its completion will reset the interval.
            return;
        }

        reminderRequestInFlight = true;
        long requestStartedNanos = System.nanoTime();
        try {
            reminderExecutor.execute(() -> {
                long queryStartedNanos = System.nanoTime();
                Optional<Word> selected = Optional.empty();
                SQLException queryError = null;
                try {
                    selected = wordRepository.findNextForReview(
                            settingsRepository.getReminderLevel(), settingsRepository.getReminderCategory());
                } catch (SQLException ex) {
                    queryError = ex;
                }
                long queryFinishedNanos = System.nanoTime();
                Optional<Word> result = selected;
                SQLException failure = queryError;

                postToFx(() -> {
                    reminderRequestInFlight = false;
                    if (exiting) return;

                    long uiQueueDelayMillis = TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - queryFinishedNanos);
                    if (uiQueueDelayMillis >= 2_000L) {
                        AppLogger.warn("Hatırlatma sonucu JavaFX kuyruğunda " + uiQueueDelayMillis
                                + " ms bekledi; ana arayüz yükü kontrol edilmeli.");
                    }
                    if (activePopup != null) {
                        reminderPendingWhilePopupOpen = true;
                        return;
                    }
                    if (failure != null) {
                        AppLogger.error("Hatırlatma için sıradaki kelime alınamadı.", failure);
                        showError("Kelime getirilemedi", failure.getMessage());
                        scheduler.retryAfterInterval();
                        return;
                    }
                    if (result.isEmpty()) {
                        scheduler.retryAfterInterval();
                        return;
                    }

                    try {
                        long popupBuildStartedNanos = System.nanoTime();
                        Word word = result.get();
                        presentationRecordedForActivePopup = false;
                        activePopup = new ReminderPopup(word, action -> handlePopupAction(word, action));
                        activePopup.show();
                        long displayedAtNanos = System.nanoTime();
                        long workerQueueMillis = TimeUnit.NANOSECONDS.toMillis(
                                queryStartedNanos - requestStartedNanos);
                        long databaseMillis = TimeUnit.NANOSECONDS.toMillis(
                                queryFinishedNanos - queryStartedNanos);
                        long totalMillis = TimeUnit.NANOSECONDS.toMillis(
                                displayedAtNanos - requestStartedNanos);
                        long popupMillis = TimeUnit.NANOSECONDS.toMillis(
                                displayedAtNanos - popupBuildStartedNanos);
                        AppLogger.info("Hatırlatma kartı gösterildi; veritabanı kuyruğu=" + workerQueueMillis
                                + " ms, veritabanı=" + databaseMillis
                                + " ms, JavaFX kuyruğu=" + uiQueueDelayMillis
                                + " ms, kart oluşturma/gösterme=" + popupMillis
                                + " ms, toplam istek süresi=" + totalMillis + " ms.");
                        scheduler.reminderDisplayed();
                    } catch (RuntimeException ex) {
                        activePopup = null;
                        AppLogger.error("Hatırlatma kartı oluşturulamadı veya gösterilemedi.", ex);
                        scheduler.retryAfterInterval();
                    }
                });
            });
        } catch (RejectedExecutionException ex) {
            reminderRequestInFlight = false;
            if (!exiting) {
                AppLogger.error("Hatırlatma veritabanı görevi kuyruğa alınamadı.", ex);
                scheduler.retryAfterInterval();
            }
        }
    }

    private void handlePopupAction(Word word, String action) {
        // A card is counted only after the user interacts with it. A card that times out
        // untouched is deliberately excluded from presentation statistics.
        if (!"AUTO_TIMEOUT".equals(action) && !presentationRecordedForActivePopup) {
            presentationRecordedForActivePopup = true;
            recordUserSeenPresentation(word);
        }

        // Hard-word toggles and the reveal action are non-terminal.
        boolean terminalAction = switch (action) {
            case "HARD", "UNHARD", "INTERACTED" -> false;
            default -> true;
        };
        if (terminalAction) {
            activePopup = null;
        }

        switch (action) {
            case "KNOWN" -> {
                executeDatabaseMutation(
                        "Kelime öğrenildi olarak kaydedilemedi.",
                        () -> wordRepository.recordReview(word.id(), "KNOWN"));
                resumePendingReminderIfNeeded();
            }
            case "REPEAT" -> {
                reminderPendingWhilePopupOpen = false;
                scheduler.snoozeToConfiguredInterval();
                executeDatabaseMutation(
                        "Kelime tekrar kararı kaydedilemedi.",
                        () -> wordRepository.recordReview(word.id(), "REPEAT"));
            }
            case "HARD" -> executeDatabaseMutation(
                    "Zor kelime işareti kaydedilemedi.",
                    () -> wordRepository.setHardWord(word.id(), true));
            case "UNHARD" -> executeDatabaseMutation(
                    "Zor kelime işareti kaldırılamadı.",
                    () -> wordRepository.setHardWord(word.id(), false));
            case "DISMISS", "AUTO_TIMEOUT" -> resumePendingReminderIfNeeded();
            case "INTERACTED" -> { }
            default -> {
                scheduler.retryAfterInterval();
                resumePendingReminderIfNeeded();
            }
        }
    }

    private void recordUserSeenPresentation(Word word) {
        try {
            reminderExecutor.execute(() -> {
                try {
                    wordRepository.recordPresentation(word.id());
                } catch (SQLException ex) {
                    AppLogger.error("Kullanıcının etkileşimde bulunduğu kartın gösterimi kaydedilemedi.", ex);
                }
            });
        } catch (RejectedExecutionException ex) {
            AppLogger.error("Kelime kartı gösterim kaydı kuyruğa alınamadı.", ex);
        }
    }

    private void resumePendingReminderIfNeeded() {
        if (reminderPendingWhilePopupOpen && !exiting) {
            reminderPendingWhilePopupOpen = false;
            scheduler.triggerNow();
        }
    }

    /** Executes short SQLite writes away from JavaFX and refreshes the UI asynchronously. */
    private void executeDatabaseMutation(String errorMessage, SqlMutation mutation) {
        if (reminderExecutor == null || reminderExecutor.isShutdown()) return;
        try {
            reminderExecutor.execute(() -> {
                try {
                    mutation.run();
                    postToFx(() -> {
                        if (!exiting && mainWindow != null) mainWindow.refreshData();
                    });
                } catch (SQLException ex) {
                    AppLogger.error(errorMessage, ex);
                    postToFx(() -> {
                        if (!exiting) {
                            showError("İşlem kaydedilemedi", ex.getMessage());
                            scheduler.retryAfterInterval();
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            AppLogger.error("Hatırlatma veritabanı işlemi kuyruğa alınamadı.", ex);
        }
    }

    private void postToFx(Runnable action) {
        try {
            Platform.runLater(action);
        } catch (IllegalStateException ex) {
            AppLogger.warn("JavaFX platformu kapandığı için arayüz güncellemesi gönderilemedi.");
        }
    }

    @FunctionalInterface
    private interface SqlMutation {
        void run() throws SQLException;
    }

    private void exitApplication() {
        if (exiting) return;
        exiting = true;
        if (scheduler != null) scheduler.close();
        if (activePopup != null) activePopup.close();
        if (databaseExecutor != null) databaseExecutor.shutdownNow();
        if (reminderExecutor != null) reminderExecutor.shutdownNow();
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
        if (databaseExecutor != null) databaseExecutor.shutdownNow();
        if (reminderExecutor != null) reminderExecutor.shutdownNow();
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
