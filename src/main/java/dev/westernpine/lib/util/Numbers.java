package dev.westernpine.lib.util;

public class Numbers {

    public static boolean isWithin(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int setWithin(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static boolean isWithin(long value, long min, long max) {
        return value >= min && value <= max;
    }

    public static long setWithin(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

}
