package dev.westernpine.lib.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonUtils {

    public static List<JsonElement> find(JsonElement base, String identifier) {
        List<JsonElement> values = new ArrayList<>();
        if (base == null || base.isJsonNull())
            return values;
        if (base.isJsonArray()) {
            for (JsonElement arrayEl : base.getAsJsonArray()) {
                values.addAll(find(arrayEl, identifier));
            }
        }
        if (base.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entrySet = base.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getKey().equals(identifier)) {
                    values.add(entry.getValue());
                }
                values.addAll(find(entry.getValue(), identifier));
            }
        } else {
            if (base.toString().equals(identifier)) {
                values.add(base);
            }
        }
        return values;
    }

    public static JsonElement getJson(String urlString) throws IOException {
        URLConnection request = new URL(urlString).openConnection();
        request.connect();
        return JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
    }

    public static JsonElement getJson(Map<String, String> headers, String urlString) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(false);
        headers.forEach(con::setRequestProperty);
        con.connect();
        return JsonParser.parseReader(new InputStreamReader((InputStream) con.getContent()));
    }

}
