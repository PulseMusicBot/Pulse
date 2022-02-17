package dev.westernpine.pulse.controller.settings.setting;

import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformFactory;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;

import java.util.UUID;

public class Setting {

    public static final Setting DEFAULT_VOLUME = SettingFactory.from(
            UUID.fromString("1d5c55e4-f4eb-4eae-9af1-28f052c73c92"),
            ":loud_sound:",
            "Default Volume",
            "The volume to set on join.",
            "7",
            "defaultvolume");

    public static final Setting IMAGE_SIZE = SettingFactory.from(
            UUID.fromString("96897d32-95a0-4944-a29a-006e22844e60"),
            ":frame_with_picture:",
            "Thumbnails",
            "Thumbnail sizes.",
            TriState.NONE.toString(),
            "imagesize");

    public static final Setting DEFAULT_PLATFORM = SettingFactory.from(
            UUID.fromString("359d1ffd-4ca2-47ed-be9d-e3f36980e747"),
            ":station:",
            "Default Search Platform",
            "The default platform to search on.",
            PlatformManager.defaultPlatform().getSourceName(),
            "defaultplatform");

    public static final Setting SHUFFLE_PLAYLISTS = SettingFactory.from(
            UUID.fromString("c1f44a6a-b4d9-4053-8b91-ddf6a0b38985"),
            ":twisted_rightwards_arrows:",
            "Shuffle Playlists",
            "Shuffle playlists when they are enqueued.",
            "false",
            "shuffleplaylists");

    public static final Setting DJ_MODE = SettingFactory.from(
            UUID.fromString("9ba8c3fd-728d-4b53-bf19-49d693d18463"),
            ":dvd:",
            "DJ Mode",
            "Restricts experience-modifying actions to DJs only.",
            "false",
            "djmode");

    public static final Setting DJ_ROLE = SettingFactory.from(
            UUID.fromString("81a0d76c-5134-495e-88cc-4adcd46c830f"),
            ":scroll:",
            "DJ Role",
            "The role to attach DJ permissions to.",
            "",
            "djrole");

    public static final Setting VOICE_DETECTION = SettingFactory.from(
            UUID.fromString("51e15b72-23ca-496f-a640-74ab9bead334"),
            ":microphone:",
            "Voice Detection",
            "Suppress audio when others talk.",
            "false",
            "voicedetection");

    public static final Setting TRACK_UPDATES = SettingFactory.from(
            UUID.fromString("452698c1-8bf8-4a51-af8b-ac69a660ac8d"),
            ":speech_balloon:",
            "Track Updates",
            "Send a track change notification.",
            "true",
            "trackupdates");

    public static final Setting DISCONNECT_CLEANUP = SettingFactory.from(
            UUID.fromString("923fba84-4512-444b-ab05-549e58e0e8fe"),
            ":outbox_tray:",
            "Disconnect Cleanup",
            "Remove a member's requests when the leave.",
            "false",
            "disconnectcleanup");

    public static final Setting TWENTY_FOUR_SEVEN = SettingFactory.from(
            UUID.fromString("091f984b-0576-43a8-bc59-c3e1185b6335"),
            ":watch:",
            "24/7",
            "Always stay connected, and loop current queue.",
            "false",
            "247");

    public static final Setting JOIN_MUSIC = SettingFactory.from(
            UUID.fromString("9ba9f638-0f18-4ef3-9283-7cc6fe628b26"),
            ":inbox_tray:",
            "Join Music",
            "The music to use when joining without a request.",
            "",
            "joinmusic");

    private final UUID uuid;
    private final String emoji;
    private final String label;
    private final String description;
    private final String defaultValue;
    private final String[] ids;

    public Setting(UUID uuid, String emoji, String label, String description, String defaultValue, String... ids) {
        this.uuid = uuid;
        this.emoji = emoji;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
        this.ids = ids == null ? new String[0] : ids;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String[] getIds() {
        return ids;
    }

}
