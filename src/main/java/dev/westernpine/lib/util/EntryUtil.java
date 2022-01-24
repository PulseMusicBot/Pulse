package dev.westernpine.lib.util;

import java.util.Map;
import java.util.function.Function;

public class EntryUtil {

    public static <K, V, N> Map.Entry<N, V> remapKey(Map.Entry<K, V> entry, Function<K, N> keyRemapper) {
        return new Map.Entry<N, V>() {
            final N n = keyRemapper.apply(entry.getKey());

            @Override
            public N getKey() {
                return n;
            }

            @Override
            public V getValue() {
                return entry.getValue();
            }

            @Override
            public V setValue(V value) {
                return entry.setValue(value);
            }
        };
    }

    public static <K, V, N> Map.Entry<K, N> remapValue(Map.Entry<K, V> entry, Function<V, N> valueRemapper) {
        return new Map.Entry<K, N>() {
            N n = valueRemapper.apply(entry.getValue());

            @Override
            public K getKey() {
                return entry.getKey();
            }

            @Override
            public N getValue() {
                return n;
            }

            @Override
            public N setValue(N value) {
                N old = n;
                n = value;
                return old;
            }
        };
    }

}
