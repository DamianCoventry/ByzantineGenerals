public class Majority {
    private int _indoor = 0, _outdoor = 0;

    public void accumulate(Message m) {
        if (m._activity == LoyalFriend.Activity.OUTDOOR) ++_outdoor;
        else ++_indoor;
    }

    public LoyalFriend.Activity get() {
        return _outdoor > _indoor ? LoyalFriend.Activity.OUTDOOR : LoyalFriend.Activity.INDOOR;
    }
}
