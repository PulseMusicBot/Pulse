package dev.westernpine.lib.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitterTest {

    @Test
    void split() {
        int pageSize = 5;
        Collection<Integer> ints = Arrays.asList(
        1
                ,2
                ,3
                ,4
                ,5

                ,1
                ,2
                ,3
                ,4
                ,5

                ,6
        );
        List<List<Integer>> split = Splitter.split(ints, pageSize);

        System.out.println(split.toString());

    }
}