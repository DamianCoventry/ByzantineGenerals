import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            final Random random = new Random();
            final Messenger messenger = new Messenger();
            final Thread[] threads = new Thread[Friend.NUM_FRIENDS];

            for (int id = 0; id < Friend.NUM_FRIENDS; ++id) {
                threads[id] = new Friend(id, random, messenger);
                threads[id].start();
            }

            for (var x : threads) {
                x.join();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
