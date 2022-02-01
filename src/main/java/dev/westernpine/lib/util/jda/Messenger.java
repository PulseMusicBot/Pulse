package dev.westernpine.lib.util.jda;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.Scheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Messenger extends RestUtil {

    /**
     * Checks if the self member has write permissions.
     *
     * @param channel The channel to check against.
     * @return True if the self member has write permissions.
     */
    public static boolean hasWritePermissions(@Nullable TextChannel channel) {
        if (channel == null) return false;
        return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND);
    }

    /**
     * Checks if the self member has embed permissions.
     *
     * @param channel The channel to check against.
     * @return True if the self member has embed permissions.
     */
    public static boolean hasEmbedPermissions(@Nullable TextChannel channel) {
        if (channel == null) return false;
        return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
    }

    /**
     * Checks if the self member has manage permissions.
     *
     * @param channel The channel to check against.
     * @return True if the self member has manage permissions.
     */
    public static boolean hasManagePermissions(@Nullable TextChannel channel) {
        if (channel == null) return false;
        return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);
    }

    /**
     * Converts the given object into a Message object.
     *
     * @param messageableObject The object to transform.
     * @return A MessageBuilder object.
     */
    public static MessageBuilder messageBuilder(@Nonnull Object messageableObject) {
        if (messageableObject instanceof EmbedBuilder embedBuilder) //Done first as it's the strictest type.
            return new MessageBuilder(embedBuilder);
        if (messageableObject instanceof MessageEmbed messageEmbed) //Done before Message check, see below.
            return new MessageBuilder(messageEmbed);
        if (messageableObject instanceof Message message) //Done second to last because messageableObject could also be an instance of MessageEmbed which can be an instance of Message.
            return new MessageBuilder(message);
        return new MessageBuilder(messageableObject.toString()); //Done last as a failsafe/default action.
    }

    /**
     * Format a given message into a string that looks like an embed, or an error message if the resulting string is too long.
     *
     * @param message The message to format.
     * @return A Message object in string form to look like an embed.
     */
    public static Message formatEmbed(Message message) {
        if (message.getEmbeds().isEmpty()) {
            return messageBuilder(message.getContentRaw()).build();
        } else {
            MessageEmbed embed = message.getEmbeds().get(0);
            StringBuilder string = new StringBuilder();
            if (embed.getTitle() != null)
                string.append("__**").append(embed.getTitle()).append("**__").append("\n");
            if (embed.getDescription() != null)
                string.append(embed.getDescription());
            embed.getFields().forEach(field -> string.append("__**").append(field.getName()).append("**__\n").append(field.getValue()).append("\n"));
            if (embed.getFooter() != null)
                string.append("\n").append("_").append(embed.getFooter().getText()).append("_");
            String out = string.toString();
            if (string.length() > 1024)
                out = "This message is longer than 1024 characters, please give me `MESSAGE_EMBED_LINKS` permission and try again";
            return messageBuilder(out).build();
        }
    }

    /**
     * Gets the MessageAction for sending a message in a channel.
     *
     * @param channel The channel to send the message in.
     * @param message The message to send.
     * @return The MessageAction for sending a message in a channel.
     */
    public static RestAction<?> getDeleteAction(TextChannel channel, Message message) {
        if (message.getAuthor().getId().equals(channel.getJDA().getSelfUser().getId()) || hasManagePermissions(channel))
            return message.delete();
        return channel.getGuild().retrieveOwner().complete().getUser().openPrivateChannel().complete().sendMessageEmbeds(Embeds.error("No permissions!", String.format("I am unable to delete messages in %s on your guild!", channel.getAsMention())).build());
    }

    /**
     * Gets the MessageAction for sending a message in a channel.
     *
     * @param channel The channel to send the message in.
     * @param message The message to send.
     * @return The MessageAction for sending a message in a channel.
     */
    public static MessageAction getSendAction(TextChannel channel, Message message) {
        if (hasWritePermissions(channel))
            if (hasEmbedPermissions(channel) || message.getEmbeds().isEmpty())
                return channel.sendMessage(message);
            else
                return channel.sendMessage(formatEmbed(message));
        return channel.getGuild().retrieveOwner().complete().getUser().openPrivateChannel().complete().sendMessageEmbeds(Embeds.error("No permissions!", String.format("I am unable to write in %s on your guild!", channel.getAsMention())).build());
    }

    /**
     * Gets the MessageAction for editing a message in a channel.
     *
     * @param previousMessage The message to be edited.
     * @param message         The new message to be set.
     * @return The MessageAction for editing a message in a channel.
     */
    public static MessageAction getEditAction(Message previousMessage, Message message) {
        TextChannel channel = previousMessage.getTextChannel();
        if (hasWritePermissions(channel))
            if (hasEmbedPermissions(channel))
                return previousMessage.editMessage(message);
            else
                return previousMessage.editMessage(formatEmbed(message));
        return channel.getGuild().retrieveOwner().complete().getUser().openPrivateChannel().complete().sendMessageEmbeds(Embeds.error("No permissions!", String.format("I am unable to write in %s on your guild!", channel.getAsMention())).build());
    }

    /**
     * Gets the MessageAction for replying to an interaction.
     *
     * @param event   The event to reply to.
     * @param message The message to reply with.
     * @return The MessageAction for replying to an interaction.
     */
    public static ReplyAction getReplyAction(GenericInteractionCreateEvent event, Message message) {
        TextChannel channel = event.getTextChannel();
        if (hasEmbedPermissions(channel) || message.getEmbeds().isEmpty())
            return event.reply(message);
        else
            return event.reply(formatEmbed(message));
    }

    /**
     * Try to delete a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param message The message to delete.
     * @return A completable future containing nothing if successful, a Message if unable to manage messages, or any exceptions if failed.
     */
    public static CompletableFuture<?> deleteMessage(TextChannel channel, Message message) {
        Try<RestAction<?>> restfulActionTry = Try.to(() -> getDeleteAction(channel, message));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<? super Object> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(completableFuture::complete, completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to delete a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param message The message to delete.
     * @param deltime The time in seconds before attempting to delete the message.
     * @return A completable future containing nothing if successful, a Message if unable to manage messages, or any exceptions if failed.
     */
    public static CompletableFuture<?> deleteMessage(TextChannel channel, Message message, long deltime) {
        Try<RestAction<?>> restfulActionTry = Try.to(() -> getDeleteAction(channel, message));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<? super Object> completableFuture = new CompletableFuture<>();
        Scheduler.after(deltime, TimeUnit.SECONDS).thenAccept(no -> restfulActionTry.getUnchecked().queue(completableFuture::complete, completableFuture::completeExceptionally));
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param channel       The channel to send the message in.
     * @param messageObject The message to send.
     * @return A completable future containing the message sent if successful, or any exceptions if failed.
     */
    public static CompletableFuture<Message> sendMessage(TextChannel channel, Object messageObject) {
        Message messageToSend = messageBuilder(messageObject).build();
        Try<MessageAction> restfulActionTry = Try.to(() -> getSendAction(channel, messageToSend));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<Message> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(completableFuture::complete, completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param channel       The channel to send the message in.
     * @param messageObject The message to send.
     * @param deltime       The time in seconds before attempting to delete the message sent.
     * @return A completable future containing nothing if successful, or any exceptions if failed.
     */
    public static CompletableFuture<?> sendMessage(TextChannel channel, Object messageObject, long deltime) {
        Message messageToSend = messageBuilder(messageObject).build();
        Try<MessageAction> restfulActionTry = Try.to(() -> getSendAction(channel, messageToSend));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<? super Object> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(message ->
                        Try.to(() -> deleteMessage(channel, message, deltime).get())
                                .onSuccess(completableFuture::complete)
                                .onFailure(completableFuture::completeExceptionally),
                completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param previousMessage The message to edit.
     * @param messageObject   The new message to set.
     * @return A completable future containing the message if successful, or any exceptions if failed.
     */
    public static CompletableFuture<Message> editMessage(Message previousMessage, Object messageObject) {
        Message newMessage = messageBuilder(messageObject).build();
        Try<MessageAction> restfulActionTry = Try.to(() -> getEditAction(previousMessage, newMessage));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<Message> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(completableFuture::complete, completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param previousMessage The message to edit.
     * @param messageObject   The new message to set.
     * @param deltime         The time in seconds before attempting to delete the original reply.
     * @return A completable future containing nothing if successful, or any exceptions if failed.
     */
    public static CompletableFuture<?> editMessage(Message previousMessage, Object messageObject, long deltime) {
        Message newMessage = messageBuilder(messageObject).build();
        Try<MessageAction> restfulActionTry = Try.to(() -> getEditAction(previousMessage, newMessage));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<? super Object> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(message ->
                        Try.to(() -> deleteMessage(message.getTextChannel(), message, deltime).get())
                                .onSuccess(completableFuture::complete)
                                .onFailure(completableFuture::completeExceptionally),
                completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param event         The interaction event to reply to.
     * @param messageObject The new message to set.
     * @return A completable future containing the message if successful, or any exceptions if failed.
     */
    public static CompletableFuture<InteractionHook> replyTo(GenericInteractionCreateEvent event, Object messageObject) {
        Message responseMessage = messageBuilder(messageObject).build();
        Try<RestAction<InteractionHook>> restfulActionTry = Try.to(() -> getReplyAction(event, responseMessage));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<InteractionHook> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(completableFuture::complete, completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }

    /**
     * Try to send a message in a channel, and get the result in the form of a CompletableFuture.
     *
     * @param event         The interaction event to reply to.
     * @param messageObject The new message to set.
     * @param deltime       The time in seconds before attempting to delete the original reply.
     * @return A completable future containing nothing if successful, or any exceptions if failed.
     */
    public static CompletableFuture<?> replyTo(GenericInteractionCreateEvent event, Object messageObject, long deltime) {
        Message responseMessage = messageBuilder(messageObject).build();
        Try<RestAction<InteractionHook>> restfulActionTry = Try.to(() -> getReplyAction(event, responseMessage));
        if (!restfulActionTry.isSuccessful())
            return CompletableFuture.failedFuture(restfulActionTry.getFailureCause());
        CompletableFuture<? super Object> completableFuture = new CompletableFuture<>();
        restfulActionTry.getUnchecked().queue(interactionHook ->
                        Try.to(() -> interactionHook.retrieveOriginal()
                                        .queue(message ->
                                                        Try.to(() -> deleteMessage(message.getTextChannel(), message, deltime).get())
                                                                .onSuccess(completableFuture::complete)
                                                                .onFailure(completableFuture::completeExceptionally),
                                                completableFuture::completeExceptionally
                                        )
                                )
                                .onFailure(completableFuture::completeExceptionally),
                completableFuture::completeExceptionally);
        Scheduler.addRequiredCompletion(completableFuture);
        return completableFuture;
    }
}
