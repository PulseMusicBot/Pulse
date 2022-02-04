package dev.westernpine.lib.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Splitter {

    public static <T> LinkedList<T[]> split(T[] array, int chunkSize) {
        LinkedList<T[]> split = new LinkedList<>();
        for (int counter = 0; counter < array.length / chunkSize; counter++)
            split.add(Arrays.copyOfRange(array, counter * chunkSize, counter * chunkSize + chunkSize));
        return split;
    }

    public static <T> LinkedList<LinkedList<T>> split(Collection<T> collection, int chunkSize) {
        T[] array = (T[]) collection.toArray();
        LinkedList<LinkedList<T>> split = new LinkedList<>();
        for (int counter = 0; counter < collection.size() / chunkSize; counter++)
            split.add(new LinkedList<>(List.of(Arrays.copyOfRange(array, counter * chunkSize, counter * chunkSize + chunkSize))));
        return split;
    }

}
