public class Majority {
    private int _indoor = 0, _outdoor = 0;

    public void accumulate(Message m) {
        if (m._activity == Friend.Activity.OUTDOOR) ++_outdoor;
        else ++_indoor;
    }

    public Friend.Activity get() {
        return _outdoor > _indoor ? Friend.Activity.OUTDOOR : Friend.Activity.INDOOR;
    }
}
