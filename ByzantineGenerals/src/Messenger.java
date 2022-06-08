import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Messenger {
    private final static int MAX_QUEUE_DEPTH = 10;
    private final LinkedBlockingQueue<Message>[] _messages;

    @SuppressWarnings (value="unchecked")
    public Messenger() {
        _messages = new LinkedBlockingQueue[LoyalFriend.NUM_FRIENDS];
        for (int i = 0; i < LoyalFriend.NUM_FRIENDS; ++i) {
            _messages[i] = new LinkedBlockingQueue<>(MAX_QUEUE_DEPTH);
        }
    }

    public void send(int me, LoyalFriend.Activity activity) throws InterruptedException {
        for (int i = 0; i < LoyalFriend.NUM_FRIENDS; ++i) {
            // don't put the sender's message into the sender's queue
            if (i != me) {
                _messages[i].put(new Message(me, activity));
            }
        }
    }

    public Message[] receive(String name, int me, int n) throws InterruptedException {
        Message[] messages = new Message[n];
        int i = 0;
        while (i < n) {
            messages[i] = _messages[me].poll(1000, TimeUnit.MILLISECONDS); // Returns null if times out
            if (messages[i] == null) {
                System.out.println(name + ": Timed out waiting for a message.");
            }
            ++i;
        }
        return messages;
    }
}
