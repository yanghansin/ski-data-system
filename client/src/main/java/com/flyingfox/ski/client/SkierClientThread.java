package com.flyingfox.ski.client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class SkierClientThread implements Runnable{
    private final BlockingQueue<LiftRide> eventQueue;
    private final int requestsPerThread;
    private final CountDownLatch latch;
    private final SkiersApi skiersApi;
    private final Queue<String> latencyRecords;

    public SkierClientThread(BlockingQueue<LiftRide> eventQueue, int requestsPerThread, CountDownLatch latch, Queue<String> latencyRecords) {
        this.eventQueue = eventQueue;
        this.requestsPerThread = requestsPerThread;
        this.latch = latch;
        this.latencyRecords = latencyRecords;

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(System.getenv("SKI_API_BASE_PATH"));
        this.skiersApi = new SkiersApi(apiClient);
    }

    @Override
    public void run() {
        for (int i = 0; i < requestsPerThread; i++) {
            try{
                LiftRide liftRide = eventQueue.take();
                long startTime = System.currentTimeMillis();
                int responseCode = 0;
                try{
                    skiersApi.writeNewLiftRide(
                            liftRide,
                            liftRide.getResortID(),
                            liftRide.getSeasonID(),
                            liftRide.getDayID(),
                            liftRide.getSkierID()
                    );
                    responseCode = 201;
                    RequestCounter.incrementSuccessfulRequests();
                } catch (ApiException e) {
                    responseCode = e.getCode();
                    RequestCounter.incrementFailedRequests();
                }

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                latencyRecords.add(startTime + "," + endTime + ",POST," + latency + "," + responseCode );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread" + Thread.currentThread().getId() + " Interrupted: " + e.getMessage());
                break;
            }
        }
        latch.countDown();
    }
}
