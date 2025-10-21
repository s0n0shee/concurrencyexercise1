package com.orangeandbronze.concurrency;

import java.util.*;
import java.util.concurrent.*;

class TaskProcessor {

    // Processes the list of WorkerTask and returns total successful task results
    // BUG: current implementation submits tasks but does not reliably wait for completion
    int processTasks(List<WorkerTask> tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(), 10));
        List<Future<Integer>> futures = new ArrayList<>();
        for (WorkerTask t : tasks) {
            futures.add(executor.submit(t));
        }

        int total = 0;
        // BUG: simply checking isDone() can miss tasks not yet completed.
        for (Future<Integer> f : futures) {
                try {
                    total += f.get();
                } catch (ExecutionException e) {
                    // swallow in this buggy version
                    e.printStackTrace();
                }

        }

        // BUG: executor is not properly shutdown/waited upon in all cases.
        executor.shutdown();
        return total;
    }
}