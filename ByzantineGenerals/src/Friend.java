import java.util.Random;

public class Friend extends Thread {
    static final public int NUM_FRIENDS = 4;

    private final Messenger _messenger;
    private final int _me;
    private final Random _random;

    public enum Activity { INDOOR, OUTDOOR }

    public Friend(Random random, Messenger messenger, int id) {
        _random = random;
        _messenger = messenger;
        _me = id;
        setName("Friend" + _me);
    }

    @Override
    public void run() {
        try {
            Activity myActivity = randomActivity();
            sendActivity(myActivity);

            Message[] friendsActivities = receiveFriendsActivities();
            Message[] myPlan = buildPlan(myActivity, friendsActivities);
            sendPlan(myPlan);

            Message[][] friendsPlans = receiveFriendsPlans();
            Message[] majorityPlan = buildMajorityPlan(myPlan, friendsPlans);

            Activity finalPlan = determineMajority(majorityPlan);
            print("my final plan is for an " + finalPlan + " activity.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Activity randomActivity() {
        Activity activity = _random.nextBoolean() ? Activity.INDOOR : Activity.OUTDOOR;
        print("chose " + activity);
        return activity;
    }

    private void sendActivity(Activity activity) throws InterruptedException {
        print("sending " + activity + " to friends");
        _messenger.send(_me, activity);

        Thread.sleep(1000); // This makes the output easier to follow
    }

    private Message[] receiveFriendsActivities() throws InterruptedException {
        Message[] messages = _messenger.receive(_me, NUM_FRIENDS-1);
        print("received friends' activities " + toString(messages));

        Thread.sleep(1000); // This makes the output easier to follow

        return messages;
    }

    private Message[] buildPlan(Activity myActivity, Message[] friendsActivities) {
        Message[] plan = new Message[NUM_FRIENDS];
        for (Message m : friendsActivities) {
            plan[m._friend] = m;
        }
        plan[_me] = new Message(_me, myActivity);
        return plan;
    }

    private void sendPlan(Message[] myPlan) throws InterruptedException {
        print("sending my plan to friends");
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                _messenger.send(i, myPlan[i]._activity);
            }
        }
    }

    private Message[][] receiveFriendsPlans() throws InterruptedException {
        Message[][] friendsPlans = new Message[NUM_FRIENDS][];
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                friendsPlans[i] = _messenger.receive(i, NUM_FRIENDS-1);
                print("received friend" +i+ "'s plan which is " + toString(friendsPlans[i]));
            }
        }

        Thread.sleep(1000); // This makes the output easier to follow
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

        Thread.sleep(1000); // This makes the output easier to follow
        return majorityPlan;
    }

    private Friend.Activity determineFriendsMajority(int friend, Message myPlanForFriend, Message[][] friendsPlanForFriend) {
        // First consider what our plan is for this friend
        Majority majority = new Majority();
        majority.accumulate(myPlanForFriend);

        // Now consider what the plan that each friend has for this friend
        for (Message[] messages : friendsPlanForFriend) {
            if (messages == null) { // needed because of the "i != _me" statements above
                continue;
            }
            for (Message m : messages) {
                if (m._friend == friend) {
                    majority.accumulate(m);
                }
            }
        }

        return majority.get();
    }

    private String toString(Message[] messages) {
        StringBuilder s = new StringBuilder("[");
        for (Message m : messages) {
            if (s.length() > 1) s.append(", ");
            s.append("(").append(m._friend).append(", ").append(m._activity).append(")");
        }
        return s + "]";
    }

    private Friend.Activity determineMajority(Message[] majorityPlan) {
        int indoor = 0, outdoor = 0;
        for (Message message : majorityPlan) {
            if (message._activity == Activity.OUTDOOR) ++outdoor;
            else ++indoor;
        }
        return outdoor > indoor ? Activity.OUTDOOR : Activity.INDOOR;
    }

    private void print(String s) {
        System.out.println(getName() + ": " + s);
    }
}
