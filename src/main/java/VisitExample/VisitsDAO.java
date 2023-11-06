package VisitExample;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import java.util.*;
import java.util.stream.Collectors;


public class VisitsDAO {

    private static final String TableName = "visits";
    public static final String IndexName = "visits-index";

    private static final String VisitorAttr = "visitor";
    private static final String LocationAttr = "location";

    // DynamoDB client
    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();

    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }



    /**
     * Retrieve the number of times visitor has visited location
     *
     * @param visitor
     * @param location
     * @return
     */
    public int getVisitCount(String visitor, String location) {
        DynamoDbTable<Visit> table = enhancedClient.table(TableName, TableSchema.fromBean(Visit.class));
        Key key = Key.builder()
                .partitionValue(visitor).sortValue(location)
                .build();

        Visit visit = table.getItem(key);
        return visit == null ? 0 : visit.getVisit_count();
    }

    /**
     * Increment the number of times visitor has visited location
     *
     * @param visitor
     * @param location
     */
    public void recordVisit(String visitor, String location) {
        DynamoDbTable<Visit> table = enhancedClient.table(TableName, TableSchema.fromBean(Visit.class));
        Key key = Key.builder()
                .partitionValue(visitor).sortValue(location)
                .build();

        // load it if it exists
        Visit visit = table.getItem(key);
        if(visit != null) {
            visit.setVisit_count(visit.getVisit_count() + 1);
            table.updateItem(visit);
        } else {
            Visit newVisit = new Visit();
            newVisit.setVisitor(visitor);
            newVisit.setLocation(location);
            newVisit.setVisit_count(1);
            table.putItem(newVisit);
        }
    }

    /**
     * Delete all visits of visitor to location
     *
     * @param visitor
     * @param location
     */
    public void deleteVisit(String visitor, String location) {
        DynamoDbTable<Visit> table = enhancedClient.table(TableName, TableSchema.fromBean(Visit.class));
        Key key = Key.builder()
                .partitionValue(visitor).sortValue(location)
                .build();
        table.deleteItem(key);
    }

    /**
     * Fetch the next page of locations visited by visitor
     *
     * @param visitor The visitor of interest
     * @param pageSize The maximum number of locations to include in the result
     * @param lastLocation The last location returned in the previous page of results
     * @return The next page of locations visited by visitor
     */
    public DataPage<Visit> getVisitedLocations(String visitor, int pageSize, String lastLocation) {
        DynamoDbTable<Visit> table = enhancedClient.table(TableName, TableSchema.fromBean(Visit.class));
        Key key = Key.builder()
                .partitionValue(visitor)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(pageSize);

        if(isNonEmptyString(lastLocation)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(VisitorAttr, AttributeValue.builder().s(visitor).build());
            startKey.put(LocationAttr, AttributeValue.builder().s(lastLocation).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<Visit> result = new DataPage<Visit>();

        PageIterable<Visit> pages = table.query(request);
        pages.stream()
                .limit(1)
                .forEach((Page<Visit> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(visit -> result.getValues().add(visit));
                });

        return result;
    }

    /**
     * Fetch the next page of visitors who have visited location
     *
     * @param location The location of interest
     * @param pageSize The maximum number of visitors to include in the result
     * @param lastVisitor The last visitor returned in the previous page of results
     * @return The next page of visitors who have visited location
     */
    public DataPage<Visit>  getVisitors(String location, int pageSize, String lastVisitor) {
        DynamoDbIndex<Visit> index = enhancedClient.table(TableName, TableSchema.fromBean(Visit.class)).index(IndexName);
        Key key = Key.builder()
                .partitionValue(location)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(pageSize);

        if(isNonEmptyString(lastVisitor)) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(LocationAttr, AttributeValue.builder().s(location).build());
            startKey.put(VisitorAttr, AttributeValue.builder().s(lastVisitor).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<Visit> result = new DataPage<Visit>();

        SdkIterable<Page<Visit>> sdkIterable = index.query(request);
        PageIterable<Visit> pages = PageIterable.create(sdkIterable);
        pages.stream()
                .limit(1)
                .forEach((Page<Visit> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(visit -> result.getValues().add(visit));
                });

        return result;
    }

}
