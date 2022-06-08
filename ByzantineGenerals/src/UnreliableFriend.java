import java.util.Random;

public class UnreliableFriend extends LoyalFriend {
    public UnreliableFriend(Random random, Messenger messenger, int id) {
        super(random, messenger, id);
        setName("UnreliableFriend" + id);
    }

    @Override
    protected void sendActivity(Activity activity) throws InterruptedException {
        // This friend is unreliable because she/he doesn't always send their chosen activity
        if (_random.nextBoolean()) {
            super.sendActivity(activity);
        }
    }

    @Override
    protected void sendPlan(Message[] myPlan) throws InterruptedException {
        print("sending my plan to friends");
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                // This friend is unreliable because she/he doesn't always send their plan
                if (_random.nextBoolean()) {
                    _messenger.send(i, myPlan[i]._activity);
                }
            }
        }
    }
}
