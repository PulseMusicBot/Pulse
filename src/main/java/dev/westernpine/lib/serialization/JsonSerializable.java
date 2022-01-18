package dev.westernpine.lib.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
/*
We let each class decide how to serialize and deserialize itself, as there may be some special cases each class needs to take into account (such as lists of foreign objects that need to be specially serialized).
We also deal with strings here instead of JsonObjects, as we want the classes themselves to take care of all the work. This will leave out code cleaner.
 */
public interface JsonSerializable {

    public static final Gson gson = new Gson();

    public String serialize();

    public void deserialize(String json);
}
