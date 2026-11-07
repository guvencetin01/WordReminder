package com.wordreminder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ReminderSchedulerTest {

    @Test
    void triggerNowDispatchesOneReminder() throws InterruptedException {
        CountDownLatch fired = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();

        try (ReminderScheduler scheduler = new ReminderScheduler(() -> {
            calls.incrementAndGet();
            fired.countDown();
        })) {
            scheduler.triggerNow();

            assertTrue(fired.await(2, TimeUnit.SECONDS), "Immediate reminder should be dispatched");
            assertEquals(1, calls.get());
        }
    }

    @Test
    void snoozeReplacesThePreviouslyScheduledReminder() throws InterruptedException {
        CountDownLatch fired = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();

        try (ReminderScheduler scheduler = new ReminderScheduler(() -> {
            calls.incrementAndGet();
            fired.countDown();
        })) {
            scheduler.start(1);
            scheduler.snooze(Duration.ofMillis(100));

            assertTrue(fired.await(3, TimeUnit.SECONDS), "Snoozed reminder should be dispatched");
            Thread.sleep(150);
            assertEquals(1, calls.get(), "The replaced one-minute task must not also run");
        }
    }

    @Test
    void closeCancelsPendingReminder() throws InterruptedException {
        CountDownLatch fired = new CountDownLatch(1);

        ReminderScheduler scheduler = new ReminderScheduler(fired::countDown);
        scheduler.snooze(Duration.ofMillis(250));
        scheduler.close();

        assertFalse(fired.await(400, TimeUnit.MILLISECONDS),
                "A closed scheduler must not dispatch its pending reminder");
    }
}
