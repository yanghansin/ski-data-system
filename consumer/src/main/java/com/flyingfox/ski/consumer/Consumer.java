package com.flyingfox.ski.consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.LiftRide;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer {
    private static final String QUEUE_NAME = "lift_ride_queue";
    private static final Gson gson = new Gson();
    private static final int THREAD_COUNT = 200;
    private static final List<LiftRide> buffer = Collections.synchronizedList(new ArrayList<>());
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    private static final LiftRideDaoDynamoDB dao = new LiftRideDaoDynamoDB();


    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        factory.setUsername(System.getenv().getOrDefault("RABBITMQ_USER", "guest"));
        factory.setPassword(System.getenv().getOrDefault("RABBITMQ_PASS", "guest"));
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);


        Connection connection = factory.newConnection();
        Thread flusher = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    List<LiftRide> toInsert;
                    synchronized (buffer) {
                        if (buffer.isEmpty()) continue;
                        toInsert = new ArrayList<>(buffer);
                        buffer.clear();
                    }
                    dao.insertLiftRide(toInsert);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        flusher.start();
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    channel.basicQos(10);

                    System.out.println("Waiting for message Consumption ... ");
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        LiftRide liftRide = gson.fromJson(message, LiftRide.class);
                        synchronized (buffer) {
                            buffer.add(liftRide);
                        }

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    };
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }

    }
}
