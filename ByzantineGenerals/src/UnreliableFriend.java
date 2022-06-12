import java.util.Random;

// This friend doesn't always respond. They randomly choose to put no message into the messenger, instead opting to
// print a debug message only.

public class UnreliableFriend extends LoyalFriend {
    public UnreliableFriend(int id, Random random, Messenger messenger) {
        super(id, random, messenger);
        setName("UnreliableFriend" + _me);
    }

    @Override
    protected void sendPlanToOthers(Activity[] plan) {
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != this._me) {
                if (_random.nextBoolean()) { // should we respond?
                    _messenger.send(id, _me, _me, plan[_me], Message.Round._1);  // synchronised
                    print(TRACE, "sent " + plan[_me] + " to Friend" + id + " for Friend" + _me);
                }
                else {
                    print(DEBUG, "not sending my plan to Friend" + id);
                }
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
                    if (_random.nextBoolean()) { // should we respond?
                        _messenger.send(idB, _me, idA, plan[idA], Message.Round._2);  // synchronised
                        print(TRACE, "sent " + plan[idA] + " to Friend" + idB + " for Friend" + idA);
                    }
                    else {
                        print(DEBUG, "not sending Friend" + idA + "'s plan to Friend" + idB);
                    }
                }
            }
        }
    }
}
