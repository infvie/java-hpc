package simpleMath;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Compute {
    public record MultiExecutor(List<PowerCalculatingThread> threads) {

        public BigInteger sum() {
            for (Thread thread : threads) thread.start();

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            BigInteger result = BigInteger.ZERO;
            for (PowerCalculatingThread thread : threads) {
                result = result.add(thread.getResult());
            }
            return result;
        }
    }


    private static class PowerCalculatingThread extends Thread {
        private BigInteger result = BigInteger.ONE;
        private final BigInteger base;
        private final BigInteger pow;

        public PowerCalculatingThread (int base, int pow) {
            this.base = BigInteger.valueOf(base);
            this.pow = BigInteger.valueOf(pow);
        }

        @Override
        public void run() {
            for (BigInteger i = BigInteger.ZERO; i.compareTo(pow) != 0; i = i.add(BigInteger.ONE)) {
                result = result.multiply(base);
            }
        }

        public BigInteger getResult() {
            return result;
        }
    }
    public static void main(String[] args) {
        List<PowerCalculatingThread> tasks = new ArrayList<>();
        tasks.add(new PowerCalculatingThread(2,3));
        tasks.add(new PowerCalculatingThread(3,3));

        MultiExecutor executor = new MultiExecutor(tasks);

        System.out.println(executor.sum());
    }
}
