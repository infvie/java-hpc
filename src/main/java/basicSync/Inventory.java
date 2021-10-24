package basicSync;

import java.util.concurrent.locks.Lock;

public class Inventory {

    public static class InventoryCounter {
        private static int count = 0;

        public void increment() {
            count++;
        }

        public void decrement() {
            count--;
        }
        public int getCount() {
            return count;
        }
    }

    public static class AddInventory extends Thread {
        private InventoryCounter counter;

        public AddInventory (InventoryCounter counter) {
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
        private InventoryCounter counter;

        public RemoveInventory (InventoryCounter counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i <= 10000; i++) {
                counter.decrement();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InventoryCounter counter = new InventoryCounter();
        AddInventory adder = new AddInventory(counter);
        RemoveInventory remove = new RemoveInventory(counter);

        adder.start(); remove.start();
        adder.join(); remove.join();

        // this count will be unstable due to the data race
        System.out.println("The number left after this is " + counter.getCount());
        // We can fix this in multiple ways
        // 1. We can add the synchronized keyword to the increment and decrement methods
        SyncInventory.main();
        // 2. We can use a basic lock
        Locked.main();
        // Note that volatile and atomic int will not solve this problem since we have multiple threads


    }
}
