package nl.pancompany.spaceinvaders.shared;

import java.util.concurrent.atomic.AtomicInteger;

public class Count {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int times;

    private Count(int times) {
        this.times = times;
    }

    public static Count times(int times) {
        return new Count(times);
    }

    public boolean finished() {
        int currentCount = counter.incrementAndGet();
        if (currentCount == times) {
            counter.set(0);
        }
        return currentCount == times;
    }
}
