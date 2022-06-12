import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Friend extends Thread {
    public static final int NUM_FRIENDS = 4;

    public enum Activity { INDOOR, OUTDOOR, DID_NOT_RESPOND }
    private final int _me;
    private final Random _random;
    private final Messenger _messenger;

    // Log level constants
    private static final int BRIEF = 1;     // prints initial choice and final plan. Fewest log messages.
    private static final int INFO = 2;      // prints received plans and majority plans
    private static final int DEBUG = 3;     // prints reported and majority plan counts
    private static final int TRACE = 4;     // prints all sent and received messages. Most log messages.

    private final int _logLevel = INFO; // <--- change this to see more or fewer log messages

    public Friend(int id, Random random, Messenger messenger) {
        _me = id;
        _random = random;
        _messenger = messenger;
        setName("Friend" + _me);
    }

    @Override
    public void run() {
        try {
            // Randomly choose an activity
            Activity[] plan = new Activity[NUM_FRIENDS];
            plan[_me] = _random.nextBoolean() ? Activity.INDOOR : Activity.OUTDOOR;
            print(BRIEF, "initial choice " + plan[_me]);

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

    private void sendPlanToOthers(Activity[] plan) {
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != this._me) {
                _messenger.send(id, _me, _me, plan[_me], Message.Round._1);  // synchronised
                print(TRACE, "sent " + plan[_me] + " to Friend" + id + " for Friend" + _me);
            }
        }
    }

    @SuppressWarnings("BusyWait")
    private void receivePlanFromOthers(Activity[] plan) throws InterruptedException {
        final LinkedList<Integer> responses = new LinkedList<>();
        for (int id = 0; id < NUM_FRIENDS; ++id) {
            if (id != _me) responses.add(id);
        }

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

    private void sendOthersPlansToOthers(Activity[] plan) {
        for (int idA = 0; idA < NUM_FRIENDS; ++idA) {
            if (idA == this._me) {
                continue;
            }
            for (int idB = 0; idB < NUM_FRIENDS; ++idB) {
                if (idB != this._me && idB != idA) {
                    _messenger.send(idB, _me, idA, plan[idA], Message.Round._2);  // synchronised
                    print(TRACE, "sent " + plan[idA] + " to Friend" + idB + " for Friend" + idA);
                }
            }
        }
    }

    private static class Pair {
        public int _from;
        public int _forWhom;
        public Pair(int from, int forWhom) {
            _from = from;
            _forWhom = forWhom;
        }
    }

    @SuppressWarnings("BusyWait")
    private Activity[][] receiveOthersPlansFromOthers() throws InterruptedException {
        final LinkedList<Pair> responses = new LinkedList<>();
        for (int idA = 0; idA < NUM_FRIENDS; ++idA) {
            if (idA == this._me) {
                continue;
            }
            for (int idB = 0; idB < NUM_FRIENDS; ++idB) {
                if (idB != this._me && idB != idA) {
                    responses.add(new Pair(idB, idA));
                }
            }
        }

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

    private void print(int logLevel, String s) {
        if (logLevel <= _logLevel) {
            System.out.println(getName() + ": " + s);
        }
    }
}
