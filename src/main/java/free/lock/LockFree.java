package free.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class LockFree {

    private static class StackNode<T> {
        public T value;
        public StackNode<T> next;

        public StackNode(T value) {
            this.value = value;
            this.next = next;
        }
    }

    public static class BlockStack<T> {
        private StackNode<T> head;
        private int counter = 0;

        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value);
            newHead.next = head;
            head = newHead;
            counter++;
        }

        public synchronized T pop() {
            if (head == null) {
                counter++;
                return null;
            }
            T value = head.value;
            head = head.next;
            counter++;
            return value;
        }

        public int getCounter() { return counter; }
    }

    public static class LockFreeStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value);

            while (true) {
                StackNode<T> currentHead = head.get();
                newHeadNode.next = currentHead;

                if (head.compareAndSet(currentHead, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHead = head.get();
            StackNode<T> newHead;

            while (currentHead != null) {
                newHead = currentHead.next;
                if (head.compareAndSet(currentHead,newHead)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                    currentHead = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHead != null ? currentHead.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static void perfBlockStack() throws InterruptedException {
        BlockStack<Integer> stack = new BlockStack<>();

        Random random = new Random();

        for (int i = 0; i <= 10000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int numThreads = 2;

        for (int i=0; i <= numThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);

            thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);

        }

        for (Thread thread : threads) { thread.start(); }

        Thread.sleep(3000);
        System.out.println(stack.getClass().getSimpleName() + " performed "+ stack.getCounter() +  " operations in 3 seconds.");
    }

    public static void perfLockFreeStack() throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        Random random = new Random();

        for (int i = 0; i <= 10000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int numThreads = 2;

        for (int i=0; i <= numThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);

            thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);

        }

        for (Thread thread : threads) { thread.start(); }

        Thread.sleep(3000);
        System.out.println(stack.getClass().getSimpleName() + " performed "+ stack.getCounter() +  " operations in 3 seconds.");
    }

    public static void main(String[] args) throws InterruptedException {
        perfBlockStack();
        perfLockFreeStack();
    }
}
