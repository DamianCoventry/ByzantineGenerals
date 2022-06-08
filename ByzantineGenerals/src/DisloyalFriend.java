import java.util.Random;

public class DisloyalFriend extends LoyalFriend {
    public DisloyalFriend(Random random, Messenger messenger, int id) {
        super(random, messenger, id);
        setName("DisloyalFriend" + id);
    }

    @Override
    protected void sendActivity(Activity ignore) throws InterruptedException {
        // This friend is disloyal because she/he sends a random activity each time
        super.sendActivity(randomActivity());
    }

    @Override
    protected void sendPlan(Message[] myPlan) throws InterruptedException {
        print("sending my plan to friends");
        for (int i = 0; i < NUM_FRIENDS; ++i) {
            if (i != _me) {
                // This friend is disloyal because she/he sends a random activity each time
                _messenger.send(i, randomActivity());
            }
        }
    }
}
