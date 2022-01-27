package dev.westernpine.lib.util.jda;

import com.vdurmont.emoji.EmojiParser;
import dev.westernpine.lib.object.Colors;
import dev.westernpine.lib.object.Timestamp;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Embeds extends Messenger {

    /**
     * Creates a success embed.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @return A success EmbedBuiler.
     */
    public static EmbedBuilder success(String title, String description) {
        return new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode(":heavy_check_mark: %s".formatted(title)))
                .setDescription(EmojiParser.parseToUnicode(description))
                .setColor(Colors.GREEN);
    }

    /**
     * Creates an error embed.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @return A error EmbedBuiler.
     */
    public static EmbedBuilder error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode(":cross_mark: %s".formatted(title)))
                .setDescription(EmojiParser.parseToUnicode(description))
                .setColor(Colors.RED);
    }

    /**
     * Creates an info embed.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @param color       The color of the embed.
     * @return A info EmbedBuiler.
     */
    public static EmbedBuilder info(String title, String description, Color color) {
        return new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode("%s".formatted(title)))
                .setDescription(EmojiParser.parseToUnicode(description))
                .setColor(color);
    }

    /**
     * Creates a play embed.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @param duration    The duration of the track.
     * @param color       The color of the embed.
     * @return A play EmbedBuiler.
     */
    public static EmbedBuilder play(String title, String description, long duration, Color color) {
        return new EmbedBuilder()
                .setTitle(EmojiParser.parseToUnicode(":musical_note: %s".formatted(title)))
                .setDescription(EmojiParser.parseToUnicode(description))
                .setFooter("Duration: %s".formatted(duration > -1 ? new Timestamp(TimeUnit.MILLISECONDS, duration).toSmallFrameStamp() : "Live"), null)
                .setColor(color);
    }

    /**
     * Creates a small embed.
     *
     * @param description The description of the embed.
     * @param color       The color of the embed.
     * @return A small EmbedBuiler.
     */
    public static EmbedBuilder small(String description, Color color) {
        return new EmbedBuilder()
                .setDescription(EmojiParser.parseToUnicode(description))
                .setColor(color);
    }

}
