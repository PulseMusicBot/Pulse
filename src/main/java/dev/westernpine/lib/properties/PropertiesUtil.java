package dev.westernpine.lib.properties;

import dev.westernpine.bettertry.Try;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesUtil {

    /**
     * Creates a new file if it doesn't exist.
     *
     * @param fileName The name of the file.
     * @return True if the file existed, false otherwise.
     * @throws Throwable Wrapper of possible IOException in the case where an IO exception occurs while creating a new file that doesn't exist.
     */
    public static boolean createIfNotExists(String fileName) throws Throwable {
        File file = new File(fileName);
        if (!file.exists())
            return Try.to(file::createNewFile).map(result -> false).get();
        return true;
    }

    /**
     * Creates/Saves a given properties file.
     *
     * @param properties The properties to save to the file.
     * @param fileName   The file name to save the properties to.
     * @param comments   Any comments to save with the properties.
     * @throws FileNotFoundException if the specified file name doesn't exist.
     * @throws IOException           if there is a problem opening, writing to, or closing the IO stream of the file.
     */
    public static void save(Properties properties, String fileName, String... comments) throws Throwable {
        createIfNotExists(fileName);
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            properties.store(fos, comments.length > 0 ? (String.join("\n#", comments)) : fileName + " File");
        }
    }

    /**
     * Loads properties from a properties file.
     *
     * @param fileName The file name to load properties from.
     * @return A properties object containing all the saved properties.
     * @throws FileNotFoundException if the specified file name doesn't exist.
     * @throws IOException           if there is a problem opening, writing to, or closing the IO stream of the file.
     */
    public static Properties load(String fileName) throws Throwable {
        createIfNotExists(fileName);
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            properties.load(fis);
        }
        return properties;
    }

    /**
     * Set the default properties from a given map.
     *
     * @param properties The properties to be set.
     * @param defaults   The map of properties to set.
     * @return A filled-in properties object.
     */
    public static Properties setDefaults(Properties properties, Map<String, String> defaults) {
        for (Entry<String, String> entry : defaults.entrySet())
            properties.putIfAbsent(entry.getKey(), entry.getValue());
        return properties;
    }

    /**
     * Sets the missing properties of a map from another map.
     *
     * @param properties   The properties to fill.
     * @param replacements The replacement properties to use.
     * @return The re-filled missing entries.
     */
    public static Properties fillMissing(Properties properties, Properties replacements) {
        for (Entry<Object, Object> entry : replacements.entrySet())
            properties.putIfAbsent(entry.getKey(), entry.getValue());
        return properties;
    }

    /**
     * Loads any existing properties from the designated file. Sets the defaults in memory. Then saves the memory properties to the file.
     *
     * @param fileName The file name to load properties from and save properties to.
     * @param defaults The default properties.
     * @param comments Any comments to save to the properties file.
     * @return A properties object represented by the properties file.
     * @throws FileNotFoundException if the specified file name doesn't exist.
     * @throws IOException           if there is a problem opening, writing to, or closing the IO stream of the file.
     */
    public static Properties loadSetSave(String fileName, Map<String, String> defaults, String... comments) throws Throwable {
        Properties properties = setDefaults(load(fileName), defaults);
        save(properties, fileName, comments);
        return properties;
    }

}
