package com.flyingfox.ski.consumer;

import io.swagger.client.model.LiftRide;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
public class LiftRideDaoDynamoDB {
    private final DynamoDbClient ddb;
    private static final String TABLE_NAME = "SkierLiftRideTable";
    private static final int MAX_BATCH_SIZE = 25;

    public LiftRideDaoDynamoDB() {
        this.ddb = DynamoDbClient.builder().region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-west-2"))).build();
    }

    public void insertLiftRide(List<LiftRide> rides) {
        for (int i = 0; i < rides.size(); i += MAX_BATCH_SIZE) {
            int end = Math.min(i + MAX_BATCH_SIZE, rides.size());
            List<LiftRide> batch = rides.subList(i, end);

            List<WriteRequest> writeRequestList = new ArrayList<>();
            for (LiftRide ride : batch) {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("skierID", AttributeValue.builder().n(String.valueOf(ride.getSkierID())).build());
                item.put("timestamp", AttributeValue.builder().n(String.valueOf(ride.getTime())).build());
                item.put("liftID", AttributeValue.builder().n(String.valueOf(ride.getLiftID())).build());
                item.put("resortID", AttributeValue.builder().n(String.valueOf(ride.getResortID())).build());
                item.put("seasonID", AttributeValue.builder().s(ride.getSeasonID()).build());
                item.put("dayID", AttributeValue.builder().s(ride.getDayID()).build());

                writeRequestList.add(WriteRequest.builder()
                        .putRequest(PutRequest.builder().item(item).build())
                        .build());
            }

            Map<String, List<WriteRequest>> batchMap = new HashMap<>();
            batchMap.put(TABLE_NAME, writeRequestList);
            BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                    .requestItems(batchMap)
                    .build();

            try {
                BatchWriteItemResponse response = ddb.batchWriteItem(batchWriteItemRequest);
                if (!response.unprocessedItems().isEmpty()) {
                    System.err.println("⚠️ Some unprocessed items: " + response.unprocessedItems());
                } else {
                    System.out.println("✅ Batch of " + batch.size() + " written.");
                }
            } catch (DynamoDbException e) {
                System.err.println("❌ DynamoDB write failed: " + e.awsErrorDetails().errorMessage());
                e.printStackTrace();
            }
        }
    }
}
