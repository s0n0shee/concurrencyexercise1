package com.orangeandbronze.concurrency;

import java.util.*;
import java.util.concurrent.*;

class TaskProcessor {

    // Processes the list of WorkerTask and returns total successful task results
    // BUG: current implementation submits tasks but does not reliably wait for completion
    int processTasks(List<WorkerTask> tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        List<Future<Integer>> futures = new ArrayList<>();
        for (WorkerTask t : tasks) {
            futures.add(executor.submit(t));
        }

        int total = 0;
        for (Future<Integer> f : futures) {
            try {
                total += f.get(); // Waits for task to finish
            } catch (ExecutionException e) {
                e.printStackTrace(); // Better than swallowing
            }
        }

        // Properly shut down executor and wait
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.err.println("Executor did not terminate in time. Forcing shutdown...");
            executor.shutdownNow();
        }

        return total;
    }

}