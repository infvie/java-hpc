package examples.lock;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLock {

    private final static int MAX_PRICE = 1000;
    private final static int numThreads = 3;
    private final static Random random = new Random();

    public static class InventoryDatabase {
        private TreeMap<Integer,Integer> priceCountMap = new TreeMap<>();
        private ReentrantLock lock = new ReentrantLock();

        public int getItemsBetweenPrices(int lowerBound, int upperBound) {
            lock.lock();
            try {
                Integer from = priceCountMap.ceilingKey(lowerBound);
                Integer to = priceCountMap.floorKey(upperBound);

                if (from == null || to == null) {
                    return 0;
                }

                NavigableMap<Integer, Integer> rangeOfPrices = priceCountMap.subMap(from, true, to, true);

                int sum = 0;
                for (int numOfItemsAtPrice : rangeOfPrices.values()) {
                    sum += numOfItemsAtPrice;
                }

                return sum;
            } finally {
                lock.unlock();
            }
        }

        public void addItem(int price) {
            lock.lock();
            try {
                priceCountMap.merge(price, 1, Integer::sum);
            } finally {
                lock.unlock();
            }
        }

        public void removeItem(int price) {
            lock.lock();
            try {
                Integer numItems = priceCountMap.get(price);
                if (numItems == null || numItems == 1) {
                    priceCountMap.remove(price);
                } else {
                    priceCountMap.put(price,numItems-1);
                }
            } finally {
                lock.unlock();
            }
        }

    }

    public static class RWInventoryDatabase {
        private TreeMap<Integer,Integer> priceCountMap = new TreeMap<>();
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private Lock readLock = lock.readLock();
        private Lock writeLock = lock.writeLock();

        public int getItemsBetweenPrices(int lowerBound, int upperBound) {
            readLock.lock();
            try {
                Integer from = priceCountMap.ceilingKey(lowerBound);
                Integer to = priceCountMap.floorKey(upperBound);

                if (from == null || to == null) {
                    return 0;
                }

                NavigableMap<Integer, Integer> rangeOfPrices = priceCountMap.subMap(from, true, to, true);

                int sum = 0;
                for (int numOfItemsAtPrice : rangeOfPrices.values()) {
                    sum += numOfItemsAtPrice;
                }

                return sum;
            } finally {
                readLock.unlock();
            }
        }

        public void addItem(int price) {
            writeLock.lock();
            try {
                Integer numItems = priceCountMap.get(price);
                if (numItems == null) {
                    priceCountMap.put(price, 1);
                } else {
                    priceCountMap.put(price, numItems + 1);
                }
            } finally {
                writeLock.unlock();
            }
        }

        public void removeItem(int price) {
            writeLock.lock();
            try {
                Integer numItems = priceCountMap.get(price);
                if (numItems == null || numItems == 1) {
                    priceCountMap.remove(price);
                } else {
                    priceCountMap.put(price,numItems-1);
                }
            } finally {
                writeLock.unlock();
            }
        }

    }

    public static void singleLockInventory() throws InterruptedException {
        InventoryDatabase inventoryDatabase = new InventoryDatabase();

        for (int i=0; i <= 10000; i++) {
            inventoryDatabase.addItem(random.nextInt(MAX_PRICE));
        }

        Thread writer = new Thread(() ->{
            while (true) {
                inventoryDatabase.addItem(random.nextInt(MAX_PRICE));
                inventoryDatabase.removeItem(random.nextInt(MAX_PRICE));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        writer.setDaemon(true);
        writer.start();

        List<Thread> threads = new ArrayList<>();
        for (int k = 0; k <= numThreads; k++) {
            Thread thread = new Thread(()->{
                for (int i = 0; i <= 10000; i++) {
                    int upperBound = random.nextInt(MAX_PRICE);
                    int lowerBound = upperBound > 0 ? random.nextInt(upperBound) : 0;
                    inventoryDatabase.getItemsBetweenPrices(lowerBound,upperBound);
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }

        long start = System.currentTimeMillis();
        for (Thread thread : threads) {thread.start();}
        for (Thread thread : threads) {thread.join();}
        long end = System.currentTimeMillis();

        System.out.printf("%s took %d ms", inventoryDatabase.getClass().getSimpleName() , end-start);
    }

    public static void readWriteLockInventory() throws InterruptedException {
        RWInventoryDatabase inventoryDatabase = new RWInventoryDatabase();
        for (int i=0; i <= 10000; i++) {
            inventoryDatabase.addItem(random.nextInt(MAX_PRICE));
        }

        Thread rwwriter = new Thread(() ->{
            while (true) {
                inventoryDatabase.addItem(random.nextInt(MAX_PRICE));
                inventoryDatabase.removeItem(random.nextInt(MAX_PRICE));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        rwwriter.setDaemon(true);
        rwwriter.start();

        List<Thread> rwthreads = new ArrayList<>();
        for (int k = 0; k <= numThreads; k++) {
            Thread thread = new Thread(()->{
                for (int i = 0; i <= 10000; i++) {
                    int upperBound = random.nextInt(MAX_PRICE);
                    int lowerBound = upperBound > 0 ? random.nextInt(upperBound) : 0;
                    inventoryDatabase.getItemsBetweenPrices(lowerBound,upperBound);
                }
            });
            thread.setDaemon(true);
            rwthreads.add(thread);
        }

        long start = System.currentTimeMillis();
        for (Thread thread : rwthreads) {thread.start();}
        for (Thread thread : rwthreads) {thread.join();}
        long end = System.currentTimeMillis();

        System.out.printf("%s took %d ms", inventoryDatabase.getClass().getSimpleName() , end-start);
    }

    public static void main(String[] args) throws InterruptedException {
//        singleLockInventory();
        readWriteLockInventory();
    }
}
