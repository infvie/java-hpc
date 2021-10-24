package basicSync;

public class SyncInventory {

    public static class SyncInventoryCounter {
        private static int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized void decrement() {
            count--;
        }
        public int getCount() {
            return count;
        }
    }

    public static class SyncAddInventory extends Thread {
        private SyncInventoryCounter counter;

        public SyncAddInventory (SyncInventoryCounter counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i <= 10000; i++) {
                counter.increment();
            }
        }
    }

    public static class SyncRemoveInventory extends Thread {
        private SyncInventoryCounter counter;

        public SyncRemoveInventory (SyncInventoryCounter counter) {
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
        SyncInventoryCounter counter = new SyncInventoryCounter();
        SyncAddInventory adder = new SyncAddInventory(counter);
        SyncRemoveInventory remove = new SyncRemoveInventory(counter);

        adder.start(); remove.start();
        adder.join(); remove.join();

        // this count will be unstable due to the data race
        System.out.println("The number left after this is " + counter.getCount());
    }
}
