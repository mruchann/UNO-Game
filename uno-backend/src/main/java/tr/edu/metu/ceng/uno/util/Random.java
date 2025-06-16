package tr.edu.metu.ceng.uno.util;

import java.util.concurrent.ThreadLocalRandom;

public class Random {
    public static int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int getRandomNumber(int max) {
        return getRandomNumber(0, max);
    }

    public static int getRandomNumber() {
        return getRandomNumber(0, 1_000_000_000);
    }
}
