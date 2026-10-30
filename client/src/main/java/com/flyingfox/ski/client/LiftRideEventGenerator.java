package com.flyingfox.ski.client;

import io.swagger.client.model.LiftRide;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class LiftRideEventGenerator implements Runnable{
    private final BlockingQueue<LiftRide> eventQueue;
    private final int totalEvents;

    public  LiftRideEventGenerator(BlockingQueue<LiftRide> eventQueue, int totalEvents) {
        this.eventQueue = eventQueue;
        this.totalEvents = totalEvents;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalEvents; i++) {
            LiftRide liftRide = new LiftRide();
            liftRide.setSkierID(ThreadLocalRandom.current().nextInt(1, 100001));
            liftRide.setResortID(ThreadLocalRandom.current().nextInt(1, 11));
            liftRide.setLiftID(ThreadLocalRandom.current().nextInt(1, 41));
            liftRide.setSeasonID("2025");
            liftRide.setDayID("1");
            liftRide.setTime(ThreadLocalRandom.current().nextInt(1, 361));
            try {
                eventQueue.put(liftRide);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Event Generator interrupted: " + e.getMessage());
                break;
            }
        }
    }
}
