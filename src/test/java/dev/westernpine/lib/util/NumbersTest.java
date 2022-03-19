package dev.westernpine.lib.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersTest {

    @Test
    void setWithin() {
        System.out.println(Numbers.setWithin(0, 1, 5));
    }

    @Test
    void testSetWithin() {
        System.out.println(Numbers.setWithin(50, 1, 5));
    }
}