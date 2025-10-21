package com.orangeandbronze.concurrency;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class WorkerTask implements Callable<Integer> {
    private final SharedCounter counter;
    private final Semaphore semaphore;
    private final AtomicInteger currentConcurrent;
    private final AtomicInteger maxConcurrent;
    private final CyclicBarrier barrier;
    private final ConcurrentLinkedQueue<Long> startTimes;
    private final int workMillis;

    WorkerTask(SharedCounter counter,
               Semaphore semaphore,
               AtomicInteger currentConcurrent,
               AtomicInteger maxConcurrent,
               CyclicBarrier barrier,
               ConcurrentLinkedQueue<Long> startTimes,
               int workMillis) {
        this.counter = counter;
        this.semaphore = semaphore;
        this.currentConcurrent = currentConcurrent;
        this.maxConcurrent = maxConcurrent;
        this.barrier = barrier;
        this.startTimes = startTimes;
        this.workMillis = workMillis;
    }

    @Override
    public Integer call() throws Exception {

        try {
            // BUG: semaphore is acquired but not always released if exceptions occur
            semaphore.acquire();

            // wait for all tasks to be ready
            barrier.await();

            // record start time and do work
            startTimes.add(System.currentTimeMillis());

            // increment concurrent counter and update max
            currentConcurrent.incrementAndGet();
            int cur = currentConcurrent.get();
            if (cur > maxConcurrent.get()) {
                maxConcurrent.set(cur);
            }

            Thread.sleep(workMillis);
            counter.increment();
            return 1;
        } finally {
            // BUG: if an exception occurs before this point or barrier throws, semaphore may not be released;
            // trainees should ensure proper release and decrement in a finally block.
            //Thread.currentThread().interrupt();
            currentConcurrent.decrementAndGet();
            semaphore.release();
        }
    }
}