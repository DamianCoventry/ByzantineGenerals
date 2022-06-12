public class Message {
    public int _to, _from;
    public int _forWhom;
    public LoyalFriend.Activity _activity;
    public enum Round { _1, _2 }
    public Round _round;
    public Message(int to, int from, int forWhom, LoyalFriend.Activity activity, Round round) {
        _to = to;
        _from = from;
        _forWhom = forWhom;
        _activity = activity;
        _round = round;
    }
}
