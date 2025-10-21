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
    public Integer call() {
        boolean acquired = false;
        try {
            // Wait for all tasks to be ready before starting work
            barrier.await();

            semaphore.acquire();
            acquired = true;


            startTimes.add(System.currentTimeMillis());

            // increment concurrent counter and update max
            int cur = currentConcurrent.incrementAndGet();
            maxConcurrent.updateAndGet(prev -> Math.max(prev, cur));

            Thread.sleep(workMillis);
            counter.increment();
            return 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt(); // Important!
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            currentConcurrent.decrementAndGet();
            if (acquired) {
                semaphore.release();
            }
        }
    }

}