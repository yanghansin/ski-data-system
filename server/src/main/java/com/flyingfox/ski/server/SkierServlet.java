package com.flyingfox.ski.server;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.LiftRide;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

@WebServlet("/skiers/*")
public class SkierServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SkierServlet.class.getName());
    private static final Gson gson = new Gson();
    private static final String QUEUE_NAME = "lift_ride_queue";
    private ConnectionFactory factory;
    private Connection connection;
    private final BlockingQueue<Channel> channelQueue = new LinkedBlockingQueue<Channel>();
    @Override
    public void init() throws ServletException {
        factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        factory.setUsername(System.getenv().getOrDefault("RABBITMQ_USER", "guest"));
        factory.setPassword(System.getenv().getOrDefault("RABBITMQ_PASS", "guest"));
        try{
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            for (int i = 0; i < 500; i++) {
                channelQueue.add(connection.createChannel());
            }
            logger.info("Server Initialized");
        } catch (Exception e) {
            throw new ServletException("Failed to connect to RabbitMQ", e);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        BufferedReader body = req.getReader();
        String bodyStr = body.lines().reduce("", (acc, line) -> acc + line);

        if (pathInfo == null || pathInfo.isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (!urlCheck(pathParts) || !bodyCheck(bodyStr)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
            return;
        }

        LiftRide liftRide = gson.fromJson(bodyStr, LiftRide.class);
        if (liftRide == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: could not parse LiftRide");
            return;
        }
        liftRide.setResortID(Integer.parseInt(pathParts[1]));
        liftRide.setSeasonID(pathParts[3]);
        liftRide.setDayID(pathParts[5]);
        liftRide.setSkierID(Integer.parseInt(pathParts[7]));

        String message = gson.toJson(liftRide);
        try {
            Channel channel = channelQueue.take();
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            channelQueue.put(channel);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to Queue Message");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        JsonObject responseData = new JsonObject();
        responseData.addProperty("message", "Lift ride queued successfully!");
        resp.getWriter().write(responseData.toString());

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (!urlCheck(pathParts)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("DoGet Successfully Executed");
        }


    }

    private boolean urlCheck(String[] pathParts) {
        if (pathParts.length != 8) {
            return false;
        }

        if (!pathParts[2].equals("seasons") || !pathParts[4].equals("days") || !pathParts[6].equals("skiers")){
            return false;
        }

        try {
            int resortID = Integer.parseInt(pathParts[1]);
            int seasonID = Integer.parseInt(pathParts[3]);
            int dayID = Integer.parseInt(pathParts[5]);
            int skierID = Integer.parseInt(pathParts[7]);

            return urlInputRangeCheck(resortID, seasonID, dayID, skierID);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean urlInputRangeCheck(int resortID, int seasonID, int dayID, int skierID) {
        return resortID >= 1
                && resortID <= 10
                && seasonID == 2025
                && dayID == 1
                && skierID >= 1
                && skierID <= 100000;
    }


    private boolean bodyCheck(String bodyStr) {
        try {
            LiftRide liftRide = gson.fromJson(bodyStr, LiftRide.class);
            return liftRide != null
                    && liftRide.getTime() != null
                    && liftRide.getLiftID() != null
                    && liftRide.getTime() >= 1
                    && liftRide.getTime() <= 360
                    && liftRide.getLiftID() >= 1
                    && liftRide.getLiftID() <= 40;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("message", message);
        resp.setStatus(statusCode);
        resp.getWriter().write(errorResponse.toString());
    }
}
