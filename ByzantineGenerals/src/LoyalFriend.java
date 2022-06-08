import java.util.Arrays;
import java.util.Random;

public class LoyalFriend extends Thread {
    static final public int NUM_FRIENDS = 4;

    protected final Messenger _messenger;
    protected final int _me;
    protected final Random _random;
    protected boolean[] _friendResponses;

    public enum Activity { INDOOR, OUTDOOR }

    public LoyalFriend(Random random, Messenger messenger, int id) {
        _random = random;
        _messenger = messenger;
        _me = id;

        _friendResponses = new boolean[NUM_FRIENDS];
        Arrays.fill(_friendResponses, false);

        setName("LoyalFriend" + _me);
    }

    @Override
    public void run() {
        try {
            Activity myActivity = randomActivity();
            print("chose " + myActivity);
            sendActivity(myActivity);
            waitForResponses();

            Message[] friendsActivities = receiveFriendsActivities();
            Message[] myPlan = buildPlan(myActivity, friendsActivities);
            sendPlan(myPlan);
            waitForResponses();

            Message[][] friendsPlans = receiveFriendsPlans();
            waitForResponses();

            Message[] majorityPlan = buildMajorityPlan(myPlan, friendsPlans);
            waitForResponses();

            Activity finalPlan = determineFinalPlan(majorityPlan);
            print("my final plan is ------------------> " + finalPlan + " activity.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Activity randomActivity() {
        return _random.nextBoolean() ? Activity.INDOOR : Activity.OUTDOOR;
    }

    protected void sendActivity(Activity activity) throws InterruptedException {
        print("sending " + activity + " to friends");
        _messenger.send(_me, activity);

        Thread.sleep(500); // This makes the output easier to follow
    }

    private Message[] receiveFriendsActivities() throws InterruptedException {
        Message[] messages = _messenger.receive(getName(), _me, NUM_FRIENDS-1);
        print("received friends' activities " + toString(messages));

        Thread.sleep(500); // This makes the output easier to follow
        return messages;
    }

    private Message[] buildPlan(Activity myActivity, Message[] friendsActivities) {
        Message[] plan = new Message[NUM_FRIENDS];
        for (Message m : friendsActivities) {
            if (m != null) {
                plan[m._friend] = m;
            }
        }
        plan[_me] = new Message(_me, myActivity);
        return plan;
    }

    protected void sendPlan(Message[] myPlan) throws InterruptedException {
        print("sending my plan to friends");
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me && myPlan[i] != null) {  // Did we receive a message from this friend?
                _messenger.send(i, myPlan[i]._activity);
            }
        }
    }

    private Message[][] receiveFriendsPlans() throws InterruptedException {
        Message[][] friendsPlans = new Message[NUM_FRIENDS][];
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                friendsPlans[i] = _messenger.receive(getName(), i, NUM_FRIENDS-1);
                print("received friend" + i + "'s plan which is " + toString(friendsPlans[i]));
            }
        }

        Thread.sleep(500); // This makes the output easier to follow
        return friendsPlans;
    }

    private Message[] buildMajorityPlan(Message[] myPlan, Message[][] friendsPlans) throws InterruptedException {
        Message[] majorityPlan = new Message[NUM_FRIENDS];
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                majorityPlan[i] = new Message(i, determineFriendsMajority(i, myPlan[i], friendsPlans));
            }
        }

        majorityPlan[_me] = myPlan[_me];
        print("my majority plan is " + toString(majorityPlan));

        Thread.sleep(500); // This makes the output easier to follow
        return majorityPlan;
    }

    private LoyalFriend.Activity determineFriendsMajority(int friend, Message myPlanForFriend, Message[][] friendsPlanForFriend) {
        // First consider what our plan is for this friend
        Majority majority = new Majority();
        if (myPlanForFriend != null) { // we might not have received a plan from this friend
            majority.accumulate(myPlanForFriend);
        }

        // Now consider what the plan is that each friend has for this friend
        for (Message[] messages : friendsPlanForFriend) {
            if (messages == null) { // needed because of the "i != _me" statements above
                continue;
            }
            for (Message m : messages) {
                if (m != null && m._friend == friend) { // friend might not have complete data for all friends
                    majority.accumulate(m);
                }
            }
        }

        return majority.get();
    }

    private String toString(Message[] messages) {
        if (messages == null) {
            return "[null]";
        }
        StringBuilder s = new StringBuilder("[");
        for (Message m : messages) {
            if (s.length() > 1) s.append(", ");
            if (m == null) {
                s.append("(null)");
            }
            else {
                s.append("(").append(m._friend).append(", ").append(m._activity).append(")");
            }
        }
        return s + "]";
    }

    private LoyalFriend.Activity determineFinalPlan(Message[] majorityPlan) throws InterruptedException {
        Majority majority = new Majority();
        for (Message m : majorityPlan) {
            majority.accumulate(m);
        }

        Thread.sleep(2000); // This makes the output easier to follow
        return majority.get();
    }

    protected void print(String s) {
        System.out.println(getName() + ": " + s);
    }

    private synchronized void waitForResponses() throws InterruptedException {
        // monitor the _friendResponses array
        while (friendHasNotResponded()) {
            wait();
        }
    }

    private synchronized boolean friendHasNotResponded() {
        for (boolean replied : _friendResponses) {
            if (!replied) return true;
        }
        return false;
    }
}
