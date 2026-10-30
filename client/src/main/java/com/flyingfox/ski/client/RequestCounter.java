package com.flyingfox.ski.client;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounter {
    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);

    public static void incrementSuccessfulRequests() {
        successfulRequests.incrementAndGet();
    }

    public static void incrementFailedRequests() {
        failedRequests.incrementAndGet();
    }

    public static int getSuccessfulRequests() {
        return successfulRequests.get();
    }

    public static int getFailedRequests() {
        return failedRequests.get();
    }
}
