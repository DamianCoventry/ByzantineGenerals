import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            int input = 0;
            while (input != 4) {
                displayScenarioMenu();
                input = getNumberFromUser();
                switch (input) {
                    case 1 -> noFailuresScenario();
                    case 2 -> crashFailuresScenario();
                    case 3 -> byzantineFailuresScenario();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayScenarioMenu() {
        System.out.println("\nClick in this window first. Then enter a number.\n");
        System.out.println("1) run the 'no failures' scenario.");
        System.out.println("2) run the 'crash failures' scenario.");
        System.out.println("3) run the 'byzantine failures' scenario.");
        System.out.println("4) quit.");
    }

    private static void noFailuresScenario() throws InterruptedException {
        final Messenger messenger = new Messenger();
        final Random random = new Random();
        runScenario(new Thread[] {
                new LoyalFriend(0, random, messenger),
                new LoyalFriend(1, random, messenger),
                new LoyalFriend(2, random, messenger),
                new LoyalFriend(3, random, messenger)
        });
    }

    private static void crashFailuresScenario() throws InterruptedException {
        final Messenger messenger = new Messenger();
        final Random random = new Random();
        runScenario(new Thread[] {
                new UnreliableFriend(0, random, messenger),
                new LoyalFriend(1, random, messenger),
                new LoyalFriend(2, random, messenger),
                new LoyalFriend(3, random, messenger)
        });
    }

    private static void byzantineFailuresScenario() throws InterruptedException {
        final Messenger messenger = new Messenger();
        final Random random = new Random();
        runScenario(new Thread[] {
                new DisloyalFriend(0, random, messenger),
                new LoyalFriend(1, random, messenger),
                new LoyalFriend(2, random, messenger),
                new LoyalFriend(3, random, messenger)
        });
    }

    private static void runScenario(Thread[] friends) throws InterruptedException {
        for (Thread friend : friends) {
            friend.start();
        }
        for (Thread friend : friends) {
            friend.join();
        }
    }

    private static int getNumberFromUser() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            return Integer.parseInt(br.readLine());
        } catch(NumberFormatException ignore) {}
        return -1;
    }
}
