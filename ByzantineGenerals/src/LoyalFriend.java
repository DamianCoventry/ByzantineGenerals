import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// This is base class for friends. The UnreliableFriend and DisloyalFriend classes derive from this class, then
// override the two send methods within this class so that behaviour can be changed.

public class LoyalFriend extends Thread {
    public static final int NUM_FRIENDS = 4;

    public enum Activity { INDOOR, OUTDOOR, DID_NOT_RESPOND }
    protected final int _me;
    protected final Random _random;
    protected final Messenger _messenger;

    // Log level constants
    protected static final int BRIEF = 1;     // prints initial choice and final plan. Fewest log messages.
    protected static final int INFO = 2;      // prints received plans and majority plans
    protected static final int DEBUG = 3;     // prints reported and majority plan counts
    protected static final int TRACE = 4;     // prints all sent and received messages. Most log messages.

    private final int _logLevel = INFO; // <--- change this to see more or fewer log messages

    public LoyalFriend(int id, Random random, Messenger messenger) {
        _me = id;
        _random = random;
        _messenger = messenger;
        setName("LoyalFriend" + _me);
    }

    @Override
    public void run() {
        try {
            Activity[] plan = chooseRandomActivity();

            // First round -- exchange plans.
            sendPlanToOthers(plan);
            receivePlanFromOthers(plan);  // blocks until all friends respond, or timeout

            // Second round -- let each other know who received what in the first round
            sendOthersPlansToOthers(plan);
            Activity[][] reportedPlan = receiveOthersPlansFromOthers();  // blocks until all friends respond, or timeout

            // Determine the majority
            Activity[] majorityPlan = makeMajorityPlan(plan, reportedPlan);
            Activity finalPlan = majority(majorityPlan);
            print(BRIEF, "final plan " + finalPlan);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Activity[] chooseRandomActivity() {
        Activity[] plan = new Activity[NUM_FRIENDS];
        plan[_me] = randomActivity();
        print(BRIEF, "initial choice " + plan[_me]);
        return plan;
    }

    protected Activity randomActivity() {
        return _random.nextBoolean() ? Activity.INDOOR : Activity.OUTDOOR;
    }

    protected void sendPlanToOthers(Activity[] plan) {
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != this._me) { // Don't send ourselves a message
                _messenger.send(id, _me, _me, plan[_me], Message.Round._1);  // synchronised
                print(TRACE, "sent " + plan[_me] + " to Friend" + id + " for Friend" + _me);
            }
        }
    }

    @SuppressWarnings("BusyWait")
    private void receivePlanFromOthers(Activity[] plan) throws InterruptedException {
        // collect the IDs of the friends we expect to respond into a container.
        LinkedList<Integer> responses = getExpected1stRoundResponses();

        long startTime = System.nanoTime();
        final long ONE_SECOND = TimeUnit.SECONDS.toNanos(1);

        // to handle crash failures, we need to give each friend time to respond
        while (!responses.isEmpty() && System.nanoTime() - startTime < ONE_SECOND) {
            for (int i = 0; i < responses.size(); ++i) {
                Message m = _messenger.receive(_me, responses.get(i), responses.get(i), Message.Round._1);  // synchronised
                if (m != null) {
                    plan[m._forWhom] = m._activity;
                    responses.remove(i);
                    print(TRACE, "received " + m._activity + " for Friend" + m._forWhom + " from Friend" + m._from);
                    break;
                }
            }

            Thread.sleep(100);
        }

        // if there are IDs remaining then they're for friends that didn't respond before 1 second elapsed
        for (int id : responses) {
            plan[id] = Activity.DID_NOT_RESPOND;
        }

        print(INFO, "received plans " + Arrays.toString(plan));
    }

    private LinkedList<Integer> getExpected1stRoundResponses() {
        // For the first round, we expect responses from everyone else
        final LinkedList<Integer> ids = new LinkedList<>();
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != _me) ids.add(id);
        }
        return ids;
    }

    protected void sendOthersPlansToOthers(Activity[] plan) {
        for (int idA = 0; idA < NUM_FRIENDS; ++idA) {
            if (idA == this._me) {
                continue; // Don't send ourselves a message
            }
            for (int idB = 0; idB < NUM_FRIENDS; ++idB) {
                if (idB != this._me && idB != idA) { // Exclude us, and prevent the friend sending to themselves
                    _messenger.send(idB, _me, idA, plan[idA], Message.Round._2);  // synchronised
                    print(TRACE, "sent " + plan[idA] + " to Friend" + idB + " for Friend" + idA);
                }
            }
        }
    }

    @SuppressWarnings("BusyWait")
    private Activity[][] receiveOthersPlansFromOthers() throws InterruptedException {
        // collect the IDs of the friends we expect to respond into a container.
        LinkedList<Pair> responses = getExpected2ndRoundResponses();

        long startTime = System.nanoTime();
        final long ONE_SECOND = TimeUnit.SECONDS.toNanos(1);

        Activity[][] reportedPlan = new Activity[NUM_FRIENDS][NUM_FRIENDS];

        // to handle crash failures, we need to give each friend time to respond
        while (!responses.isEmpty() && System.nanoTime() - startTime < ONE_SECOND) {
            for (Pair r : responses) {
                Message m = _messenger.receive(_me, r._from, r._forWhom, Message.Round._2);  // synchronised
                if (m != null) {
                    reportedPlan[m._forWhom][m._from] = m._activity;
                    responses.remove(r);
                    print(TRACE, "received " + m._activity + " for Friend" + m._forWhom + " from Friend" + m._from);
                    break;
                }
            }

            Thread.sleep(100);
        }

        // if there are IDs remaining then they're for friends that didn't respond before 1 second elapsed
        for (Pair r : responses) {
            reportedPlan[r._forWhom][r._from] = Activity.DID_NOT_RESPOND;
        }

        print(INFO, "received reportedPlan " + toString(reportedPlan));
        return reportedPlan;
    }

    private static class Pair {
        public int _from;
        public int _forWhom;
        public Pair(int from, int forWhom) {
            _from = from;
            _forWhom = forWhom;
        }
    }

    private LinkedList<Pair> getExpected2ndRoundResponses() {
        // For the second round, we expect two responses from everyone else.
        // How that works out is as follows:
        //  - There are four friends.
        //  - We don't send a message to ourselves.
        //  - That leaves 3 possible responses.
        //  - We don't expect a friend to send a message about themselves.
        //  - That leaves 2 expected responses.
        final LinkedList<Pair> ids = new LinkedList<>();
        for (int idA = 0; idA < NUM_FRIENDS; ++idA) {
            if (idA == this._me) {
                continue; // Don't send ourselves a message
            }
            for (int idB = 0; idB < NUM_FRIENDS; ++idB) {
                if (idB != this._me && idB != idA) { // Don't include us, or the friend
                    ids.add(new Pair(idB, idA));
                }
            }
        }
        return ids;
    }

    private String toString(Activity[][] reportedPlan) {
        final StringBuilder sb = new StringBuilder("(");
        for (Activity[] a : reportedPlan) {
            sb.append(Arrays.toString(a));
        }
        return sb.append(")").toString();
    }

    private Activity[] makeMajorityPlan(Activity[] plan, Activity[][] reportedPlan) {
        Activity[] majorityPlan = new Activity[NUM_FRIENDS];
        Arrays.fill(majorityPlan, Activity.DID_NOT_RESPOND);

        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != this._me) {
                majorityPlan[id] = majority(plan[id], id, reportedPlan);
            }
        }

        majorityPlan[_me] = plan[_me];
        print(INFO, "majorityPlan is " + Arrays.toString(majorityPlan));
        return majorityPlan;
    }

    private Activity majority(Activity activity, int id, Activity[][] reportedPlan) {
        int in = 0, out = 0, no=0;
        if (activity == Activity.INDOOR) ++in;
        else if (activity == Activity.OUTDOOR) ++out;
        else if (activity == Activity.DID_NOT_RESPOND) ++no;

        for (int i = 0; i < reportedPlan.length; ++i) {
            if (reportedPlan[id][i] == Activity.INDOOR) ++in;
            else if (reportedPlan[id][i] == Activity.OUTDOOR) ++out;
            else if (reportedPlan[id][i] == Activity.DID_NOT_RESPOND) ++no;
        }

        print(DEBUG, "counts for reportedPlan[" + id + "]: out=" + out + ", in=" + in + ", no=" + no);
        return out > in ? Activity.OUTDOOR : Activity.INDOOR;
    }

    private Activity majority(Activity[] majorityPlan) {
        int in = 0, out = 0, no=0;

        for (Activity activity : majorityPlan) {
            if (activity == Activity.INDOOR) ++in;
            else if (activity == Activity.OUTDOOR) ++out;
            else if (activity == Activity.DID_NOT_RESPOND) ++no;
        }

        print(DEBUG, "counts for majorityPlan: out=" + out + ", in=" + in + ", no=" + no);
        return out > in ? Activity.OUTDOOR : Activity.INDOOR;
    }

    protected void print(int logLevel, String s) {
        if (logLevel <= _logLevel) {
            System.out.println(getName() + ": " + s);
        }
    }
}
