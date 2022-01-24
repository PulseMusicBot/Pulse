package dev.westernpine.pulse.listeners.system.jda;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.bettertry.Try;
import dev.westernpine.pulse.Pulse;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.annotation.Nonnull;

public class MessageDeletionRequestListener extends ListenerAdapter {

    private static final int CROSSMARK_CODEPOINT = 10060;

    //Custom private-DM & Guild-message emoji detection implementation using raw packet digestion.
    @Override
    public void onRawGateway(@Nonnull RawGatewayEvent event) {
        JsonObject packet = JsonParser.parseString(event.getPackage().toString()).getAsJsonObject();
        if (!packet.get("t").getAsString().equals("MESSAGE_REACTION_ADD"))
            return;
        JsonObject payload = packet.get("d").getAsJsonObject();
        String userId = payload.get("user_id").getAsString();
        String channelId = payload.get("channel_id").getAsString();
        String messageId = payload.get("message_id").getAsString();
        String selfId = event.getJDA().getSelfUser().getId();
        if (userId.equals(selfId))
            return;
        if (!payload.has("emoji"))
            return;
        JsonObject reactionJson = payload.get("emoji").getAsJsonObject();
        String name = reactionJson.get("name").getAsString();
        if (!reactionJson.get("id").isJsonNull()) //is possibly emote
            return;
        if (name.codePointAt(0) != CROSSMARK_CODEPOINT) //not crossmark emoji
            return;
        ShardManager manager = Pulse.shardManager;
        if (payload.has("guild_id"))
            Try.of(() -> delete(manager.getTextChannelById(channelId), messageId, selfId));
        else
            manager.retrieveUserById(userId).queue(user -> user.openPrivateChannel().queue(channel -> delete(channel, messageId, selfId)));
    }

    private void delete(MessageChannel channel, String messageId, String selfId) {
        try {
            channel.getHistoryAround(messageId, 3).queue(history -> {
                Message message = history.getMessageById(messageId);
                if (message != null && message.getAuthor() != null)
                    if (message.getAuthor().getId().equals(selfId))
                        message.delete().queue(s -> {
                        }, f -> {
                        });
            }, failure -> {
            });
        } catch (Exception e) {
        }
    }

}
