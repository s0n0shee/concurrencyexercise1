package com.orangeandbronze.concurrency;

class SharedCounter {
    // Intentionally simple implementation (not thread-safe)
    private int count = 0;

    // Trainees should make this thread-safe (e.g., synchronized or use AtomicInteger)
    public synchronized void increment() {
        count++;
    }

    public int get() {
        return count;
    }
}