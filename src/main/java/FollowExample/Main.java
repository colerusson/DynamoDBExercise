package FollowExample;

public class Main {
    public static void main(String[] args) {
        try {
            FollowDao dao = new FollowDao();

            // Grab the first page of followers for @CodyMaverick
            DataPage<Follow> page = dao.getPageOfFollowers("@CodyMaverick", 5, null);
            System.out.println("First page of followers for @CodyMaverick");
            for (Follow follow : page.getValues()) {
                System.out.println(follow.toString());
            }

            // Grab the next page of followers for @CodyMaverick
            page = dao.getPageOfFollowers("@CodyMaverick", 5, page.getLastEvaluatedKey().getFollowee_handle());
            System.out.println("Next page of followers for @CodyMaverick");
            for (Follow follow : page.getValues()) {
                System.out.println(follow.toString());
            }

            // Grab the first page of followees for @CodyMaverick
            page = dao.getPageOfFollowees("@CodyMaverick", 5, null);
            System.out.println("First page of followees for @CodyMaverick");
            for (Follow follow : page.getValues()) {
                System.out.println(follow.toString());
            }

            // Grab the next page of followees for @CodyMaverick
            page = dao.getPageOfFollowees("@CodyMaverick", 5, page.getLastEvaluatedKey().getFollower_handle());
            System.out.println("Next page of followees for @CodyMaverick");
            for (Follow follow : page.getValues()) {
                System.out.println(follow.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
