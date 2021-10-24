package create;

public class Main {

    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("Hello from thread " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // small thread design using lambdas
        Thread thread = new Thread(() -> System.out.println("Hello from " + Thread.currentThread().getName()));

        // set up exceptions for throwing error messages
        thread.setUncaughtExceptionHandler((t, e) -> System.out.println("A critical error has occurred in thread " + t.getName() + " the error was " + e.getMessage()));

        // name a thread so it is more useful to us
        thread.setName("thread with lambda");

        // how to set the priority of a thread
        // ranges from 1 (lowest) - 10 (highest)
        thread.setPriority(Thread.NORM_PRIORITY);

        System.out.println("Hello from " + Thread.currentThread().getName());
        thread.start();

        // puts a thread to sleep, so it doesn't consume any cpu resources
        Thread.sleep(1);

        // class based thread objects
        // this is my preferred method
        Thread myThread = new MyThread();
        myThread.setName("class based thread");
        myThread.start();

    }
}
