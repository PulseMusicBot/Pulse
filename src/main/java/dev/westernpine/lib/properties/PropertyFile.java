package dev.westernpine.lib.properties;

import dev.westernpine.bettertry.Try;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface PropertyFile {

    /**
     * Finds all fields (Static or non-static) designated as a property field via the {@link PropertyField} annotation, and collects it into a list.
     * A field must be of type {@link Property}, must be publicly accessible, must be static, and must be non-null, in order to be considered.
     *
     * @param clazz The class to extract the fields from.
     * @return A list of all valid Property fields.
     */
    public static List<Property> getDeclaredProperties(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(PropertyField.class))
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .map(field -> Try.to(() -> field.get(null)).orElse(null))
                .filter(Objects::nonNull)
                .filter(obj -> obj instanceof Property)
                .map(obj -> (Property) obj)
                .collect(Collectors.toList());
    }

    public PropertyFile reload() throws Throwable;

    public String get(Property property);

    public String set(Property property, String value);

}
