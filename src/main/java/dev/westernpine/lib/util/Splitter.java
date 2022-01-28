package dev.westernpine.lib.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Splitter {

    public static <T> List<T[]> split(T[] array, int chunkSize) {
        List<T[]> split = new LinkedList<>();
        for (int counter = 0; counter < array.length / chunkSize; counter++)
            split.add(Arrays.copyOfRange(array, counter * chunkSize, counter * chunkSize + chunkSize));
        return split;
    }

}
