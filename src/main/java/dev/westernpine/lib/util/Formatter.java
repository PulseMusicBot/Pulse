package dev.westernpine.lib.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.westernpine.lib.audio.track.userdata.requester.Requester;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Formatter {

    /**
     * Formats given audio track info into a readable, and stylized string.
     * @param audioTrackInfo Audio track info to format.
     * @return Formatted and stylized representation of the track info.
     */
    public static String formatInfo(AudioTrackInfo audioTrackInfo) {
        return "[__**%s**__](%s)\n[*%s*](%s)".formatted(audioTrackInfo.title, audioTrackInfo.uri, audioTrackInfo.author, audioTrackInfo.uri);
    }

    /**
     * Format the requester object as an identifiable user.
     * @param requester The requester to format.
     * @return A formatted representation of the requester.
     */
    public static String formatRequester(Requester requester) {
        return "%s#%s".formatted(requester.getName(), requester.getDiscriminator());
    }

    /**
     * Format the user object as an identifiable user.
     * @param user The user to format.
     * @return A formatted representation of the user.
     */
    public static String formatUser(User user) {
        return "%s#%s".formatted(user.getName(), user.getDiscriminator());
    }

    /**
     * Format the member object as an identifiable user.
     * @param member The member to format.
     * @return A formatted representation of the member.
     */
    public static String formatMember(Member member) {
        return formatUser(member.getUser());
    }

    /**
     * Format a role to a readable string.
     * @param role The role to format.
     * @return A formatted representation of a given role.
     */
    public static String formatRole(Role role) {
        if (role.isMentionable()) {
            return role.getAsMention();
        } else {
            return "`%s`".formatted(role.getName());
        }
    }

    /**
     * Format a given duration and progress into a progress bar string of unicodes.
     * @param duration The overall duration.
     * @param position The current position.
     * @return A String representation of a progress bar.
     */
    public static String formatProgressBar(long duration, long position) {
        final int segments = 24;
        String[] markers = new String[segments];
        Arrays.fill(markers, "\u25ac");
        Long knobPosition = duration==-1 ? segments-1 : position/(duration/segments);
        markers[knobPosition.intValue()] = "\uD83D\uDD18";
        return String.join("", markers);
    }

}
