package pivot.wtk.test;

public class IterationTest {
    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();

        final int COUNT = 100000000;

        int i = 0;
        while (i < COUNT) {
            i++;
        }

        long t1 = System.currentTimeMillis();

        System.out.println(Math.log10(COUNT) + " " + (t1 - t0) + "; " + Math.log10(COUNT) / Math.log10(2));
    }
}
