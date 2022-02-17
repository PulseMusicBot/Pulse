package dev.westernpine.lib.player.audio.track.userdata.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import dev.westernpine.lib.player.manager.OpenAudioPlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlatformManager {

    private static List<Platform> platforms = new ArrayList<>();

    public static void register(Platform platform) {
        if (platform.getAudioSourceManager().get() != null)
            platforms.add(platform);
    }

    public static List<Platform> getPlatforms() {
        return platforms;
    }

    public static Platform getFromOfficial(String name) {
        return platforms.stream()
                .filter(platform -> platform.getOfficialName().equals(name))
                .findAny()
                .orElse(null);
    }

    public static Platform getFromSource(String sourceName) {
        return platforms.stream()
                .filter(platform -> platform.getSourceName().equals(sourceName))
                .findAny()
                .orElse(null);
    }

    public static <T extends AudioSourceManager> Platform getFromSource(Class<T> clazz) {
        return platforms.stream()
                .filter(platform -> clazz.isAssignableFrom(platform.getAudioSourceManager().get().getClass()))
                .findAny()
                .orElse(null);
    }

    public static Platform defaultPlatform() {
        return getFromSource("youtube");
    }

    public static void registerKnown(Supplier<OpenAudioPlayerManager> openAudioPlayerManagerSupplier) {
        register(PlatformFactory.from("YouTube", "youtube", "ytsearch:", "ytsimilar:", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("YouTube-Music", "youtube", "ytmsearch:", "ytsimilar:", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("SoundCloud", "soundcloud", "scsearch:", "scsimilar:", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Jamendo", "jamendo", "jmsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("MixCloud", "mixcloud", "mxsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Odysee", "odysee", "odsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("BiliBili", "bilibili", "blsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("iHeart", "iheart", "ihsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("iHeartRadio", "iheartradio", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Audioboom", "audioboom", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Bandcamp", "bandcamp", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Streamable", "streamable", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("OverClocked Remix", "ocremix", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("TuneIn", "tunein", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Reddit", "reddit", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Instagram", "instagram", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Smule", "smule", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("TikTok", "tiktok", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Vimeo", "vimeo", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Twitch", "twitch", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Clyp", "clyp", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("BandLab", "bandlab", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Newgrounds", "newgrounds", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("GetYarn", "getyarn.io", "", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Yandex", "yandex-music", "ymsearch", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Apple Music", "apple-music", "amsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Deezer", "deezer", "dzsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Napster", "napster", "npsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Spotify", "spotify", "spsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
        register(PlatformFactory.from("Tidal", "tidal", "tdsearch:", "", openAudioPlayerManagerSupplier, sourceName -> openAudioPlayerManagerSupplier.get().source(sourceName)));
    }

    public static JsonObject toJson(Platform platform) {
        if (platform == null)
            return null;
        JsonObject json = new JsonObject();
        json.addProperty("platform", platform.getSourceName());
        return json;
    }

    public static Platform fromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonNull())
            return null;
        JsonObject platform = jsonElement.getAsJsonObject();
        return getFromSource(platform.get("platform").getAsString());
    }

}
