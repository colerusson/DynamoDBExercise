package FollowExample;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Follow {
    private String follower_handle;
    private String followee_handle;
    private String follower_name;
    private String followee_name;

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = FollowDao.IndexName)
    public String getFollower_handle() {
        return follower_handle;
    }

    public void setFollower_handle(String followerHandle) {
        this.follower_handle = followerHandle;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = FollowDao.IndexName)
    public String getFollowee_handle() {
        return followee_handle;
    }

    public void setFollowee_handle(String followeeHandle) {
        this.followee_handle = followeeHandle;
    }

    public void setFollower_name(String followerName) {
        this.follower_name = followerName;
    }

    public void setFollowee_name(String followeeName) {
        this.followee_name = followeeName;
    }

    @Override
    public String toString() {
        return "Follow{" +
                "follower='" + follower_handle + '\'' +
                ", followerName='" + follower_name + '\'' +
                ", followee='" + followee_handle + '\'' +
                ", followeeName='" + followee_name + '\'' +
                '}';
    }
}
