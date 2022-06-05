public class Main {
    public static void main(String[] args) {
        try {
            final Thread[] friends = new Friend[Friend.NUM_FRIENDS];
            final Messenger messenger = new Messenger();

            for (int i = 0; i < Friend.NUM_FRIENDS; ++i) {
                friends[i] = new Friend(messenger, i);
                friends[i].start();
            }

            for (int i = 0; i < 4; ++i) {
                friends[i].join();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
