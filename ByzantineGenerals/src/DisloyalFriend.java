import java.util.Random;

// This friend always responds, but there's a chance that response will be a random choice.

public class DisloyalFriend extends LoyalFriend {
    public DisloyalFriend(int id, Random random, Messenger messenger) {
        super(id, random, messenger);
        setName("DisloyalFriend" + _me);
    }

    @Override
    protected void sendPlanToOthers(Activity[] plan) {
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != this._me) {
                if (_random.nextBoolean()) { // should we choose a random activity?
                    _messenger.send(id, _me, _me, randomActivity(), Message.Round._1);  // synchronised
                }
                else {
                    _messenger.send(id, _me, _me, plan[_me], Message.Round._1);  // synchronised
                }
                print(TRACE, "sent " + plan[_me] + " to Friend" + id + " for Friend" + _me);
            }
        }
    }

    @Override
    protected void sendOthersPlansToOthers(Activity[] plan) {
        for (int idA = 0; idA < NUM_FRIENDS; ++idA) {
            if (idA == this._me) {
                continue;
            }
            for (int idB = 0; idB < NUM_FRIENDS; ++idB) {
                if (idB != this._me && idB != idA) {
                    if (_random.nextBoolean()) { // should we choose a random activity?
                        _messenger.send(idB, _me, idA, randomActivity(), Message.Round._2);  // synchronised
                    }
                    else {
                        _messenger.send(idB, _me, idA, plan[idA], Message.Round._2);  // synchronised
                    }
                    print(TRACE, "sent " + plan[idA] + " to Friend" + idB + " for Friend" + idA);
                }
            }
        }
    }
}
