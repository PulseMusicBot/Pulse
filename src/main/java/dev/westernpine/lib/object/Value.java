package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.io.Serializable;

public class Value implements Serializable {

    private Object object;

    public Value(Object object) {
        this.object = object;
    }

    public static Value of(Object object) {
        return new Value(object);
    }

    public boolean isEmpty() {
        return object == null;
    }

    public boolean isPresent() {
        return object != null;
    }

    public Object toObject() {
        return object;
    }

    public <T> T to(Class<T> clazz) {
        return clazz.cast(object);
    }

    @Override
    public String toString() {
        return object.toString();
    }

    public Boolean toBoolean() {
        return Boolean.parseBoolean(object.toString());
    }

    public Long toLong() {
        return Long.parseLong(object.toString());
    }

    public Integer toInteger() {
        return Integer.parseInt(object.toString());
    }

    public Double toDouble() {
        return Double.parseDouble(object.toString());
    }

    public Float toFloat() {
        return Float.parseFloat(object.toString());
    }

    public Short toShort() {
        return toInteger().shortValue();
    }

    public Byte toByte() {
        return toInteger().byteValue();
    }

    public Number toNumber() {
        return Try.to(() -> (Number) this.toLong())
                .orElseTry(() -> (Number) this.toInteger())
                .orElseTry(() -> (Number) this.toDouble())
                .orElseTry(() -> (Number) this.toFloat())
                .orElseThrow(() -> new NumberFormatException("The value is not a number: " + object.toString()));
    }

    public TriState toTriState() {
        return TriState.from(object.toString());
    }

}
