package FollowExample;

import VisitExample.Visit;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class FollowDao {
    private static final String TableName = "follows";
    public static final String IndexName = "follows_index";
    public static final String FollowerAttr = "follower_handle";
    public static final String FolloweeAttr = "followee_handle";

    // DynamoDB client
    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_EAST_2)
            .build();

    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }



    /**
     * Get a follower based on follower and followee
     *
     * @param follower
     * @param followee
     * @return
     */
    public Follow getFollower(String follower, String followee) {
        DynamoDbTable<Follow> table = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class));
        Key key = Key.builder()
                .partitionValue(follower).sortValue(followee)
                .build();

        return table.getItem(key);
    }

    /**
     * Add a new follow to the database
     *
     * @param followerHandle
     * @param followeeHandle
     * @param followerName
     * @param followeeName
     */
    public void putFollow(String followerHandle, String followeeHandle, String followerName, String followeeName) {
        DynamoDbTable<Follow> table = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class));
        Follow newFollow = new Follow();
        newFollow.setFollower_handle(followerHandle);
        newFollow.setFollowee_handle(followeeHandle);
        newFollow.setFollower_name(followerName);
        newFollow.setFollowee_name(followeeName);
        table.putItem(newFollow);
    }

    /**
     * Update a  follow to the database
     *
     * @param followerHandle
     * @param followeeHandle
     * @param followerName
     * @param followeeName
     */
    public void updateFollow(String followerHandle, String followeeHandle, String followerName, String followeeName) {
        DynamoDbTable<Follow> table = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class));
        Key key = Key.builder()
                .partitionValue(followerHandle).sortValue(followeeHandle)
                .build();

        // update the follow
        Follow follow = table.getItem(key);
        follow.setFollower_name(followerName);
        follow.setFollowee_name(followeeName);
        table.updateItem(follow);
    }

    /**
     * Delete all followees of a follower
     *
     * @param followerHandle
     * @param followeeHandle
     */
    public void deleteFollow(String followerHandle, String followeeHandle) {
        DynamoDbTable<Follow> table = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class));
        Key key = Key.builder()
                .partitionValue(followerHandle).sortValue(followeeHandle)
                .build();
        table.deleteItem(key);
    }

    /**
     * Fetch the next page of followers for a user
     *
     * @param targetUserAlias The user of interest
     * @param pageSize The maximum number of locations to include in the result
     * @param lastUserAlias The last user returned in the previous page of results
     * @return The next page of followers for the user
     */
    public DataPage<Follow> getPageOfFollowers(String targetUserAlias, int pageSize, String lastUserAlias) {
        DynamoDbTable<Follow> table = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class));
        Key key = Key.builder()
                .partitionValue(targetUserAlias)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(pageSize);

        if (isNonEmptyString(lastUserAlias)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(FollowerAttr, AttributeValue.builder().s(targetUserAlias).build());
            startKey.put(FolloweeAttr, AttributeValue.builder().s(lastUserAlias).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<Follow> result = new DataPage<Follow>();

        PageIterable<Follow> pages = table.query(request);
        try {
            pages.stream()
                    .limit(1)
                    .forEach((Page<Follow> page) -> {
                        result.setHasMorePages(page.lastEvaluatedKey() != null);
                        page.items().forEach(follow -> result.getValues().add(follow));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Fetch the next page of followers for a user
     *
     * @param targetUserAlias The user of interest
     * @param pageSize The maximum number of locations to include in the result
     * @param lastUserAlias The last user returned in the previous page of results
     * @return The next page of followers for the user
     */
    public DataPage<Follow> getPageOfFollowees(String targetUserAlias, int pageSize, String lastUserAlias) {
        DynamoDbIndex<Follow> index = enhancedClient.table(TableName, TableSchema.fromBean(Follow.class)).index(IndexName);
        Key key = Key.builder()
                .partitionValue(targetUserAlias)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(pageSize);

        if(isNonEmptyString(lastUserAlias)) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(FolloweeAttr, AttributeValue.builder().s(targetUserAlias).build());
            startKey.put(FollowerAttr, AttributeValue.builder().s(lastUserAlias).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        DataPage<Follow> result = new DataPage<Follow>();

        SdkIterable<Page<Follow>> sdkIterable = index.query(request);
        PageIterable<Follow> pages = PageIterable.create(sdkIterable);
        pages.stream()
                .limit(1)
                .forEach((Page<Follow> page) -> {
                    result.setHasMorePages(page.lastEvaluatedKey() != null);
                    page.items().forEach(follow -> result.getValues().add(follow));
                });

        return result;
    }
}
