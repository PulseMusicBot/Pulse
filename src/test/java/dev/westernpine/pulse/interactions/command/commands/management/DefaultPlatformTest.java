package dev.westernpine.pulse.interactions.command.commands.management;

import dev.westernpine.lib.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.audio.track.userdata.platform.PlatformFactory;
import dev.westernpine.lib.util.Debug;

import java.util.Map;

class DefaultPlatformTest {

    private static final Map<String, String> choices = Map.of("youtube", Platform.YOUTUBE.getName(), "soundcloud", Platform.SOUNDCLOUD.getName());

    private static String getChoiceKey(String value) {
        return choices.entrySet().stream().filter(choice -> choice.getValue().equals(value)).map(Map.Entry::getKey).findAny().get();
    }

    public static void main(String[] args) {
        System.out.println(PlatformFactory.get(choices.get("youtube")).getPrefix());
        System.out.println(getChoiceKey(Platform.YOUTUBE.getName()));
        Debug.printStackTrace();
    }

}