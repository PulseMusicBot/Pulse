package dev.westernpine.lib.util;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesUtil {
	
	/**
	 * Creates a new file if it doesn't exist.
	 * @param fileName
	 * @return True if the file existed, false otherwise.
	 * @throws IOException 
	 */
	@SneakyThrows
	public static boolean createIfNotExists(String fileName) {
		File file = new File(fileName);
		if(!file.exists()) {
			file.createNewFile();
			return false;
		}
		return true;
	}
	
	/**
	 * Creates/Saves a given properties file.
	 * @param properties The properties to save to the file.
	 * @param fileName The file name to save the properties to.
	 * @param comments Any comments to save with the properties.
	 * @throws IOException
	 */
	@SneakyThrows
	public static void save(Properties properties, String fileName, String...comments) {
		createIfNotExists(fileName);
		FileOutputStream fos = new FileOutputStream(fileName);
		properties.store(fos, comments.length > 0 ? (String.join("\n#", comments)) : fileName + " File");
		fos.close();
	}

	/**
	 * Loads properties from a properties file.
	 * @param fileName The file name to load properties from.
	 * @return A properties object containing all the saved properties.
	 * @throws IOException
	 */
	@SneakyThrows
	public static Properties load(String fileName) {
		createIfNotExists(fileName);
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream(fileName);
		properties.load(fis);
		fis.close();
		return properties;
	}
	
	/**
	 * Set the default properties from a given map.
	 * @param properties The properties to be set.
	 * @param defaults The map of properties to set.
	 * @return A filled-in properties object.
	 */
	public static Properties setDefaults(Properties properties, Map<String, String> defaults) {
		for(Entry<String, String> entry : defaults.entrySet())
			properties.putIfAbsent(entry.getKey(), entry.getValue());
		return properties;
	}
	
	/**
	 * Sets the missing properties of a map from another map.
	 * @param properties The properties to fill.
	 * @param replacements The replacement properties to use.
	 * @return The re-filled missing entries.
	 */
	public static Properties fillMissing(Properties properties, Properties replacements) {
		for(Entry<Object, Object> entry : replacements.entrySet())
			properties.putIfAbsent(entry.getKey(), entry.getValue());
		return properties;
	}
	
	/**
	 * Loads any existing properties from the designated file. Sets the defaults in memory. Then saves the memory properties to the file.
	 * @param fileName The file name to load properties from and save properties to.
	 * @param defaults The default properties.
	 * @param comments Any comments to save to the properties file.
	 * @return A properties object represented by the properties file.
	 */
	public static Properties loadSetSave(String fileName, Map<String, String> defaults, String...comments) {
		Properties properties = load(fileName);
		properties = setDefaults(properties, defaults);
		save(properties, fileName, comments);
		return properties;
	}

}
