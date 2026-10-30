package com.flyingfox.ski.client;

import io.swagger.client.model.LiftRide;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class SkierClientMain {
    private static final int WARM_UP_THREADS = 32;
    private static final int WARM_UP_REQUESTS_PER_THREAD = 1000;
    private static final int TOTAL_EVENTS = 200_000;
    private static final int MAIN_THREADS = 500;
    private static final int REQUESTS_PER_THREAD = (TOTAL_EVENTS - WARM_UP_THREADS * WARM_UP_REQUESTS_PER_THREAD) / MAIN_THREADS;

    public static void main(String[] args) throws InterruptedException {
        if (System.getenv("SKI_API_BASE_PATH") == null) {
            System.err.println("Set SKI_API_BASE_PATH, e.g. http://localhost:8080/server");
            System.exit(1);
        }

        BlockingQueue<LiftRide> eventQueue = new ArrayBlockingQueue<>(TOTAL_EVENTS);
        Queue<String> latencyRecords = new ConcurrentLinkedQueue<>();

        System.out.println("Generating events...");
        Thread eventGeneratorThread = new Thread(new LiftRideEventGenerator(eventQueue, TOTAL_EVENTS));
        eventGeneratorThread.start();
        eventGeneratorThread.join();
        System.out.println("Events generated.");

        System.out.println("Starting warm-up phase...");
        CountDownLatch warmUpLatch = new CountDownLatch(WARM_UP_THREADS);
        ExecutorService warmUpExecutor = Executors.newFixedThreadPool(WARM_UP_THREADS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < WARM_UP_THREADS; i++) {
            warmUpExecutor.submit(new SkierClientThread(eventQueue, WARM_UP_REQUESTS_PER_THREAD, warmUpLatch, latencyRecords));
        }

        System.out.println("Waiting for any warm-up thread to complete...");
        warmUpLatch.await(1, TimeUnit.MILLISECONDS);
        System.out.println("Starting main phase...");

        CountDownLatch mainLatch = new CountDownLatch(MAIN_THREADS);
        ExecutorService mainExecutor = Executors.newFixedThreadPool(MAIN_THREADS);

        for (int i = 0; i < MAIN_THREADS; i++) {
            mainExecutor.submit(new SkierClientThread(eventQueue, REQUESTS_PER_THREAD, mainLatch, latencyRecords));
        }

        warmUpLatch.await();
        mainLatch.await();

        warmUpExecutor.shutdown();
        mainExecutor.shutdown();

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = TOTAL_EVENTS / totalTime;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("request_latency.csv"))) {
            writer.write("start_time,end_time,request_type,latency,response_code\n");
            for (String record : latencyRecords) {
                writer.write(record + "\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to write latency records: " + e.getMessage());
        }


        List<Long> latencies = new ArrayList<>();
        for (String record : latencyRecords) {
            String[] parts = record.split(",");
            latencies.add(Long.parseLong(parts[3]));
        }

        Collections.sort(latencies);
        long minLatency = latencies.get(0);
        long maxLatency = latencies.get(latencies.size() - 1);
        long medianLatency = latencies.get(latencies.size() / 2);
        long p99Latency = latencies.get((int) (latencies.size() * 0.99));

        System.out.println("==========================================");
        System.out.println("Run summary: ");
        System.out.println("Number of successful requests: " + RequestCounter.getSuccessfulRequests());
        System.out.println("Number of unsuccessful requests: " + RequestCounter.getFailedRequests());
        System.out.println("Total run time (wall time): " + totalTime + " seconds");
        System.out.println("==========================================");
        System.out.println("Latency summary: ");
        System.out.println("Mean Response Time: " + String.format ("%2f", latencies.stream().mapToLong(Long::longValue).average().orElse(0)) + " ms");
        System.out.println("Median Response Time: " + medianLatency + " ms");
        System.out.println("99th Percentile Response Time: " + p99Latency + " ms");
        System.out.println("Min Response Time: " + minLatency + " ms");
        System.out.println("Max Response Time: " + maxLatency + " ms");
        System.out.println("Total Throughput: " + String.format("%.2f", throughput) + " requests per second");
        System.out.println("==========================================");
    }
}