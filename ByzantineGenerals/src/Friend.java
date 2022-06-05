import java.util.Random;

public class Friend extends Thread {
    static final public int NUM_FRIENDS = 4;

    private final Messenger _messenger;
    private final int _me;
    private final Random _random = new Random();

    public enum Activity { INDOOR, OUTDOOR }

    public Friend(Messenger messenger, int id) {
        _messenger = messenger;
        _me = id;
        setName("Friend" + _me);
    }

    @Override
    public void run() {
        try {
            Activity activity = _random.nextBoolean() ? Activity.INDOOR : Activity.OUTDOOR;
            System.out.println("Friend" + _me + ": chose " + activity);

            // First round
            System.out.println("Friend" + _me + ": sending " + activity + " to friends");
            _messenger.sendToOthers(_me, activity);

            Thread.sleep(1000);

            Message[] plan = new Message[NUM_FRIENDS];
            Message[] messages = _messenger.receiveNMessages(_me, NUM_FRIENDS-1);
            System.out.println("Friend" + _me + ": received " + messagesToString(messages));
            for (Message m : messages) {
                plan[m._friend] = m;
            }
            plan[_me] = new Message(_me, activity);

            Thread.sleep(1000);

            // Second round
            System.out.println("Friend" + _me + ": sending friends' choices to friends");
            for (int i = 0; i < NUM_FRIENDS; ++i) {
                if (i != _me) {
                    _messenger.sendToOthers(i, plan[i]._activity);
                }
            }

            Message[][] reportedPlan = new Message[NUM_FRIENDS][];
            for (int i = 0; i < NUM_FRIENDS; ++i) {
                if (i != _me) {
                    reportedPlan[i] = _messenger.receiveNMessages(i, NUM_FRIENDS-1);
                    System.out.println("Friend" + _me + ": received friend" +i+ "'s plan which is " + messagesToString(reportedPlan[i]));
                }
            }

            Thread.sleep(1000);

            // First vote
            Message[] majorityPlan = new Message[NUM_FRIENDS];
            System.out.println("Friend" + _me + ": determining majority from friends' choices");
            for (int i = 0; i < NUM_FRIENDS; ++i) {
                if (i != _me) {
                    majorityPlan[i] = new Message(i, majority(plan[i], reportedPlan, i));
                }
            }

            Thread.sleep(1000);

            // Second vote
            majorityPlan[_me] = new Message(_me, activity);
            System.out.println("Friend" + _me + ": my current majority plan is " + messagesToString(majorityPlan));

            Thread.sleep(1000);

            Friend.Activity finalPlan = majority(majorityPlan);
            System.out.println("Friend" + _me + ": The final plan is for an " + finalPlan + " activity.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String messagesToString(Message[] messages) {
        StringBuilder s = new StringBuilder("[");
        for (Message m : messages) {
            if (s.length() > 1) s.append(", ");
            if (m == null) {
                s.append("null");
            }
            else {
                s.append("(").append(m._friend).append(", ").append(m._activity).append(")");
            }
        }
        return s + "]";
    }

    private Friend.Activity majority(Message[] messages) {
        int indoor = 0, outdoor = 0;
        for (Message message : messages) {
            if (message != null) { // needed because of the "i != _me" statements above
                if (message._activity == Activity.OUTDOOR) ++outdoor;
                else ++indoor;
            }
        }
        return outdoor > indoor ? Activity.OUTDOOR : Activity.INDOOR;
    }

    private Friend.Activity majority(Message plan, Message[][] reportedPlan, int friend) {
        int indoor = 0, outdoor = 0;
        if (plan._activity == Activity.OUTDOOR) ++outdoor;
        else ++indoor;

        for (Message[] messages : reportedPlan) {
            if (messages == null) { // needed because of the "i != _me" statements above
                continue;
            }
            for (Message m : messages) {
                if (m._friend == friend) {
                    if (m._activity == Activity.OUTDOOR) ++outdoor;
                    else ++indoor;
                }
            }
        }

        return outdoor > indoor ? Activity.OUTDOOR : Activity.INDOOR;
    }
}
