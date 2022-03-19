package dev.westernpine.lib.util;

import java.util.*;
import java.util.stream.Collectors;

public class Splitter {

    public static <T> List<List<T>> split(Collection<T> collection, int chunkSize) {
        List<T> list = new ArrayList<>(collection);
        List<List<T>> split = new ArrayList<>();
        for(int counter = 0; counter <= collection.size()-1; counter += chunkSize)
            split.add(list.subList(counter, Math.min(counter + chunkSize, list.size()))); //Don't go out of bounds! so always get max chunk index, or max list index if chunk index is out of bounds.
        return split;
    }

}
