package basicSync;

public class Locked {

    public static class LockedInventoryCounter {
        private static int count = 0;

        public void increment() {
            synchronized (this) {
                count++;
            }
        }

        public void decrement() {
            synchronized (this) {
                count--;
            }
        }
        public int getCount() {
            return count;
        }
    }

    public static class AddInventory extends Thread {
        private LockedInventoryCounter counter;

        public AddInventory (LockedInventoryCounter counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i <= 10000; i++) {
                counter.increment();
            }
        }
    }

    public static class RemoveInventory extends Thread {
        private LockedInventoryCounter counter;

        public RemoveInventory (LockedInventoryCounter counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i <= 10000; i++) {
                counter.decrement();
            }
        }
    }

    public static void main() throws InterruptedException {
        LockedInventoryCounter counter = new LockedInventoryCounter();
        AddInventory adder = new AddInventory(counter);
        RemoveInventory remove = new RemoveInventory(counter);

        adder.start(); remove.start();
        adder.join(); remove.join();

        // this count will be unstable due to the data race
        System.out.println("The number left after this is " + counter.getCount());
    }
}

