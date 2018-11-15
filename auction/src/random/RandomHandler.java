package random;

import java.util.Random;

/**
 * Simple way to share a pseudo random generator between all classes
 */
public class RandomHandler {
    private static final long DEFAULT_SEED = 1;
    private static Random r = new Random(DEFAULT_SEED);

    private RandomHandler() {
    }

    public static void set(long seed) {
        r = new Random(seed);
    }

    public static Random get() {
        return r;
    }
}
