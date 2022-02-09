package dev.westernpine.pulse.sources.iHeart;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.ThirdPartyAudioSourceManager;
import dev.westernpine.lib.audio.track.TrackFactory;
import dev.westernpine.lib.util.JsonUtils;
import dev.westernpine.lib.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iHeartAudioSourceManager extends ThirdPartyAudioSourceManager {

    public static final String CHANNEL_REGEX = "^(https://)?(www.)?iheart.com/live/(.*?)(/.*?)?$";
    public static final Pattern channelPattern = Pattern.compile(CHANNEL_REGEX);
    public static final int CHANNEL_ID_MATCHER_GROUP = 3;

    public iHeartAudioSourceManager(AudioPlayerManager playerManager) {
        this(playerManager, true);
    }

    protected iHeartAudioSourceManager(AudioPlayerManager playerManager, boolean fetchIsrc) {
        super(playerManager, fetchIsrc);
    }

    private static boolean check(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive())
            return false;
        boolean passedString = false;
        try {
            passedString = !Strings.resemblesNull(element.getAsString());
        } catch (Exception e) {
        }
        return passedString;
    }

    public static List<JsonElement> removeExceptions(List<JsonElement> elementList, String[] extentionExceptions) {
        Iterator<JsonElement> it = elementList.iterator();
        while (it.hasNext()) {
            String url = it.next().getAsString();
            for (String ex : extentionExceptions) {
                if (url.endsWith(ex)) {
                    it.remove();
                }
            }
        }
        return elementList;
    }

    @Override
    public String getSourceName() {
        return "iheartradio";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference audioReference) {
        String reference = audioReference.getIdentifier();
        Matcher channelMatcher = channelPattern.matcher(reference);

        System.out.println("Reference: " + reference);

        if (!channelMatcher.matches())
            return null;

        System.out.println("Reference passed!");

        try {

            Document doc = Jsoup.connect(reference).get();
            if (doc == null)
                return null;

            Element element = doc.getElementById("initialState");
            if (element == null)
                return null;

            String html = element.html();
            if (Strings.resemblesNull(html))
                return null;

            JsonElement base = JsonParser.parseString(html);
            if (base == null)
                return null;

            JsonElement response = JsonUtils.find(base, "responseType").get(0);
            JsonElement title = JsonUtils.find(base, "stationName").get(0);
            JsonElement author = JsonUtils.find(base, "description").get(0);
            JsonElement thumbnail = JsonUtils.find(base, "logo").get(0);

            JsonElement uri;
            List<JsonElement> elements = new ArrayList<>();
            if (elements.isEmpty())
                elements = removeExceptions(JsonUtils.find(base, "hls_stream"), new String[]{"m3u8"});
            if (elements.isEmpty())
                elements = removeExceptions(JsonUtils.find(base, "shoutcast_stream"), new String[]{});
            if (elements.isEmpty())
                elements = removeExceptions(JsonUtils.find(base, "pivot_hls_stream"), new String[]{});
            if (elements.isEmpty())
                elements = removeExceptions(JsonUtils.find(base, "shoutcast_stream"), new String[]{});
            if (elements.isEmpty())
                elements = removeExceptions(JsonUtils.find(base, "pls_stream"), new String[]{});
            if (elements.isEmpty())
                return null;
            uri = elements.get(0);

            if (!check(response) || !response.getAsString().equalsIgnoreCase("LIVE"))
                return null;
            if (!check(title))
                return null;
            if (!check(author))
                return null;
            if (!check(thumbnail))
                return null;
            if (!check(uri))
                return null;

            return TrackFactory.from(new AudioTrackInfo(title.getAsString(), author.getAsString(), -1L, channelMatcher.group(CHANNEL_ID_MATCHER_GROUP), true, reference));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void shutdown() {

    }
}
