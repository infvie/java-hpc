package vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int MAX_PASSWORD = 100;

    public static class Vault {
        private final int password;

        public Vault(int password) {
            this.password = password;
        }

        public boolean isCorrectPassword (int guess) throws InterruptedException {
            Thread.sleep(50);
            return guess == password;
        }
    }

    public static class Police extends Thread {
        @Override
        public void run() {
            for (int i = 10; i > 0; i--) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Hunt for them " + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("I caught them");
            System.exit(0);
        }
    }

    public static class Hacker extends Thread {
        protected Vault vault;

        public Hacker(Vault vault) {
            this.vault = vault;
            this.setName(this.getClass().getSimpleName());
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            System.out.println(this.getName() + " starting to hack in to the system");
            Random rng = new Random();
            while (true) {
                int guess = rng.nextInt(MAX_PASSWORD);
                try {
                    if (vault.isCorrectPassword(guess)) {
                        System.out.println("I hacked the password");
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Random pass = new Random();
        Vault vault = new Vault(pass.nextInt(MAX_PASSWORD));

        List<Thread> threads = new ArrayList<>();
        threads.add(new Hacker(vault));
        threads.add(new Police());

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
