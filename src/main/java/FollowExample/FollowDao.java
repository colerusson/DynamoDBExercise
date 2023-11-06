package FollowExample;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class FollowDao {
    private static final String TableName = "follows";
    public static final String IndexName = "follows_index";

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
        Key key = Key.builder()
                .partitionValue(followerHandle).sortValue(followeeHandle)
                .build();

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
}
