import java.util.LinkedList;

public class Messenger {
    private final LinkedList<Message>[] _messages;
    @SuppressWarnings("unchecked")
    public Messenger() {
        _messages = new LinkedList[LoyalFriend.NUM_FRIENDS];
        for (int i = 0; i < LoyalFriend.NUM_FRIENDS; ++i) {
            _messages[i] = new LinkedList<>();
        }
    }

    // protect the _messages container
    public synchronized void send(int to, int from, int forWhom, LoyalFriend.Activity activity, Message.Round round) {
        _messages[to].push(new Message(to, from, forWhom, activity, round));
    }

    // protect the _messages container
    public synchronized Message receive(int to, int from, int forWhom, Message.Round round) {
        for (Message m : _messages[to]) {
            if (m._from == from && m._forWhom == forWhom && m._round == round) {
                _messages[to].remove(m);
                return m;
            }
        }
        return null;
    }
}
