package com.wordreminder.service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Runs one reminder timer at a time. The regular interval is measured from when a card is
 * displayed, not from when the user eventually closes it. Callers defer a due tick when a
 * card is still open so reminder windows can never overlap.
 */
public final class ReminderScheduler implements AutoCloseable {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "word-reminder-scheduler");
        thread.setDaemon(true);
        return thread;
    });
    private final Runnable reminderAction;
    private ScheduledFuture<?> pendingTask;
    private int intervalMinutes = 10;
    private long generation;
    private boolean closed;

    public ReminderScheduler(Runnable reminderAction) {
        if (reminderAction == null) {
            throw new IllegalArgumentException("reminderAction cannot be null");
        }
        this.reminderAction = reminderAction;
    }

    /** Starts the first reminder after the configured interval. */
    public synchronized void start(int minutes) {
        intervalMinutes = normalizeMinutes(minutes);
        AppLogger.info("Hatırlatma zamanlayıcısı başlatıldı; aralık=" + intervalMinutes + " dakika.");
        schedule(Duration.ofMinutes(intervalMinutes));
    }

    /** Applies a newly saved interval immediately, measured from the time it is saved. */
    public synchronized void intervalChanged(int minutes) {
        intervalMinutes = normalizeMinutes(minutes);
        AppLogger.info("Hatırlatma aralığı değiştirildi; yeni aralık=" + intervalMinutes + " dakika.");
        schedule(Duration.ofMinutes(intervalMinutes));
    }

    /** Starts the next regular interval as soon as a reminder card has been shown. */
    public synchronized void reminderDisplayed() {
        schedule(Duration.ofMinutes(intervalMinutes));
    }

    /** Retries after one interval when there is no eligible word or a card is already open. */
    public synchronized void retryAfterInterval() {
        schedule(Duration.ofMinutes(intervalMinutes));
    }

    /** Temporarily replaces the regular interval with a user-selected snooze. */
    public synchronized void snooze(Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Snooze duration must be zero or positive");
        }
        schedule(duration);
    }

    /** Replaces the pending timer with an immediate reminder request. */
    public synchronized void triggerNow() {
        schedule(Duration.ZERO);
    }

    private static int normalizeMinutes(int minutes) {
        return Math.max(1, Math.min(180, minutes));
    }

    private synchronized void schedule(Duration delay) {
        if (closed || executor.isShutdown()) {
            return;
        }
        generation++;
        long taskGeneration = generation;
        if (pendingTask != null) {
            pendingTask.cancel(false);
            pendingTask = null;
        }

        long delayNanos = Math.max(0L, delay.toNanos());
        long dueAtNanos = System.nanoTime() + delayNanos;
        pendingTask = executor.schedule(
                () -> fire(taskGeneration, dueAtNanos), delayNanos, TimeUnit.NANOSECONDS);
    }

    private synchronized void fire(long taskGeneration, long dueAtNanos) {
        // Keep validation and dispatch atomic with schedule/cancel operations. This prevents
        // a just-cancelled task racing a newly requested reminder into the JavaFX queue.
        if (closed || taskGeneration != generation) {
            return;
        }
        pendingTask = null;

        long latenessMillis = Math.max(0L, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - dueAtNanos));
        if (latenessMillis >= 2_000L) {
            AppLogger.warn("Zamanlayıcı görevi " + latenessMillis + " ms geç çalıştı. "
                    + "Windows uyku/uyanma durumu veya sistem yükü etkili olmuş olabilir.");
        }
        reminderAction.run();
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        generation++;
        if (pendingTask != null) {
            pendingTask.cancel(false);
            pendingTask = null;
        }
        executor.shutdownNow();
    }
}
