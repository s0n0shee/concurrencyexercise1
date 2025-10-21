package com.orangeandbronze.concurrency;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {

    @Test
    void testConcurrencyLimit() throws Exception {
        final int TASKS = 50;
        final int MAX_CONCURRENT = 5;
        SharedCounter counter = new SharedCounter();
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT);
        AtomicInteger currentConcurrent = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(TASKS);
        ConcurrentLinkedQueue<Long> startTimes = new ConcurrentLinkedQueue<>();

        List<WorkerTask> tasks = new ArrayList<>();
        for (int i = 0; i < TASKS; i++) {
            tasks.add(new WorkerTask(counter, semaphore, currentConcurrent, maxConcurrent, barrier, startTimes, 100));
        }

        TaskProcessor processor = new TaskProcessor();
        int total = processor.processTasks(tasks);

        // Expected total = TASKS; because buggy processor may return 0, trainee must fix.
        assertEquals(TASKS, total, "Total processed tasks must equal number of tasks");

        // Ensure semaphore limited max concurrency
        assertTrue(maxConcurrent.get() <= MAX_CONCURRENT,
                "Max concurrent tasks should not exceed semaphore permits");
        assertEquals(TASKS, counter.get(), "Counter should reflect all increments");
    }

    @Test
    void testBarrierSynchronization() throws Exception {
        final int TASKS = 20;
        SharedCounter counter = new SharedCounter();
        Semaphore semaphore = new Semaphore(TASKS);
        AtomicInteger currentConcurrent = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(TASKS);
        ConcurrentLinkedQueue<Long> startTimes = new ConcurrentLinkedQueue<>();

        List<WorkerTask> tasks = new ArrayList<>();
        for (int i = 0; i < TASKS; i++) {
            tasks.add(new WorkerTask(counter, semaphore, currentConcurrent, maxConcurrent, barrier, startTimes, 50));
        }

        TaskProcessor processor = new TaskProcessor();
        int total = processor.processTasks(tasks);
        assertEquals(TASKS, total, "All tasks must complete");

        // Check that start times are roughly synchronized (within 200ms)
        List<Long> times = new ArrayList<>(startTimes);
        assertEquals(TASKS, times.size(), "All tasks should record a start time");
        long min = Collections.min(times);
        long max = Collections.max(times);
        long diff = max - min;
        assertTrue(diff <= 200, "Tasks should start within 200ms of each other due to the barrier");
    }
}