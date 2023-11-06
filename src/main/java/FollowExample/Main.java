package FollowExample;

public class Main {
    public static void main(String[] args) {
        try {
            FollowDao dao = new FollowDao();

            // Add 25 items to the follows table with same follower and different followee
            for (int i = 0; i < 25; i++) {
                dao.putFollow("@CodyMaverick", "@Followee#" + i, "Cody Maverick", "FolloweeName" + i);
            }

            // Add 25 items to the follows table with same followee and different follower
            for (int i = 0; i < 25; i++) {
                dao.putFollow("@Follower#" + i, "@CodyMaverick", "FollowerName" + i, "Cody Maverick");
            }

            //Get one of the items from the follows table using its primary key
            Follow follower = dao.getFollower("@CodyMaverick", "@Followee#1");
            System.out.println("Follower: " + follower.toString());

            //Update the follower_name and followee_name attributes of one of the items in the follows table
            dao.updateFollow("@CodyMaverick", "@Followee#1", "Cody Maverick New Name", "FolloweeName1");

            //Delete one of the items in the follows table using its primary key
            dao.deleteFollow("@CodyMaverick", "@Followee#2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
