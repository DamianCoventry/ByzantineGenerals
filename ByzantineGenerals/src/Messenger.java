import java.util.concurrent.LinkedBlockingQueue;

public class Messenger {
    private final static int MAX_QUEUE_DEPTH = 10;
    private final LinkedBlockingQueue<Message>[] _messages;

    @SuppressWarnings (value="unchecked")
    public Messenger() {
        _messages = new LinkedBlockingQueue[Friend.NUM_FRIENDS];
        for (int i = 0; i < Friend.NUM_FRIENDS; ++i) {
            _messages[i] = new LinkedBlockingQueue<>(MAX_QUEUE_DEPTH);
        }
    }

    public void sendToOthers(int me, Friend.Activity activity) throws InterruptedException {
        for (int i = 0; i < Friend.NUM_FRIENDS; ++i) {
            // don't put the sender's message into the sender's queue
            if (i != me) {
                _messages[i].put(new Message(me, activity));
            }
        }
    }

    public Message[] receiveNMessages(int me, int n) throws InterruptedException {
        Message[] messages = new Message[n];
        int i = 0;
        while (i < n) {
            messages[i++] = _messages[me].take();
        }
        return messages;
    }
}
