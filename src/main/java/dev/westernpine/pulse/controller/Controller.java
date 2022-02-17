package dev.westernpine.pulse.controller;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.player.audio.AudioFactory;
import dev.westernpine.lib.player.audio.playlist.Playlist;
import dev.westernpine.lib.player.audio.playlist.PlaylistFactory;
import dev.westernpine.lib.player.audio.track.Track;
import dev.westernpine.lib.player.audio.track.userdata.UserData;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.handlers.audio.AudioReceiver;
import dev.westernpine.pulse.controller.handlers.audio.AudioSender;
import dev.westernpine.pulse.controller.handlers.player.PlayerListener;
import dev.westernpine.pulse.controller.settings.Settings;
import dev.westernpine.pulse.controller.settings.SettingsFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.events.system.player.FinishedPlayingEvent;
import dev.westernpine.pulse.events.system.player.PlayerDestroyedEvent;
import dev.westernpine.pulse.events.system.player.PreviousQueueReachedEndEvent;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.westernpine.pulse.logging.Logger.logger;

public class Controller {

    private final String guildId;
    protected long lifetime = 0;
    protected Status status = Status.CACHE;
    protected Set<String> votesToNext = new HashSet<>();
    protected Set<String> votesToPrevious = new HashSet<>();
    private String lastChannelId;
    private boolean premium = false;
    private Settings settings;
    private Playlist previousQueue;
    private Playlist queue;
    private AudioPlayer audioPlayer;
    private AudioReceiver audioReceiver;
    private AudioSender audioSender;

    /*
    Other controller properties.
     */
    private int volume = 7;
    private boolean alone = false;
    /**
     * False = Queue, None = Off, True = Track.
     */
    private TriState repeating = TriState.NONE;
    private int lastTrack = 0;

    //TODO: update guild premium status on initialization. (requires management)

    Controller(String guildId) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = new Playlist(getGuild().getName() + "'s Previous Queue");
        this.queue = new Playlist(getGuild().getName() + "'s Queue");
    }

    public Controller(String guildId, long lifetime, Status status, String lastChannelId) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = new Playlist(getGuild().getName() + "'s Previous Queue");
        this.queue = new Playlist(getGuild().getName() + "'s Queue");
        this.lifetime = lifetime;
        this.status = status;
        this.lastChannelId = lastChannelId;
    }

    public Controller(String guildId, Playlist previousQueue, Playlist queue, long lifetime, Status status, String connectedChannel, String lastChannelId, Track track, long position, int volume, boolean paused, boolean alone, TriState repeating, int lastTrack) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = previousQueue;
        this.queue = queue;
        this.lifetime = lifetime;
        this.status = status;
        this.lastChannelId = lastChannelId;
        if (this.getGuild().getGuildChannelById(connectedChannel) instanceof AudioChannel audioChannel) {
            this.connect(audioChannel);
            this.setVolume(volume);
            this.alone = alone;
            this.repeating = repeating;
            this.lastTrack = lastTrack;
            this.setPaused(paused);
            if (track != null) {
                this.startTrack(track, true);
                this.setPosition(position);
            }
        }

        //Finally, initialize properly.
        manageStateWithStartup(true);
    }

    //Uses all connected members in the same channel.
    public void manageAlonePausing() {
        if (isConnected()) {
            if (!isPaused()) {
                if (getConnectedMembers().isEmpty() && !settings.get(Setting.TWENTY_FOUR_SEVEN).toBoolean()) {
                    setPaused(true);
                    setAlone(true);
                }
            } else {
                if (wasAlone() && !getConnectedMembers().isEmpty()) {
                    setPaused(false);
                    setAlone(false);
                }
            }
        }
    }

    //Uses all connected members in a guild.
    public void manageQueue() {
        this.removeMemberRequestsExcept(getAllConnectedMembers().stream().map(Member::getId).toList());
    }

    //Uses all connected members in a guild.
    public void manageVotes() {
        setRemainingVotes(getAllConnectedMembers().stream().map(member -> member.getUser().getId()).toList());

        if (getPlayingTrack() != null) {
            int needed = neededVotes();
            if (needed > 0) {
                int currentNext = currentVotesToNext();
                int currentPrevious = currentVotesToPrevious();
                if (currentNext >= needed && currentPrevious >= needed) {
                    getLastChannel().ifPresent(channel ->
                            Messenger.sendMessage(channel, Embeds.error("Votes to skip are tied!", "Please recast your votes, as all votes were cleared."), 15));
                    clearVotes();
                } else if (currentNext >= needed) {
                    getLastChannel().ifPresent(channel ->
                            Messenger.sendMessage(channel, Embeds.info(":arrow_right: Moving to next track.", "Enough remaining votes to move.", Pulse.color(getGuild())), 15));
                    nextTrack();
                } else if (currentPrevious >= needed) {
                    getLastChannel().ifPresent(channel ->
                            Messenger.sendMessage(channel, Embeds.info(":arrow_right: Moving to previous track.", "Enough remaining votes to move.", Pulse.color(getGuild())), 15));
                    previousTrack();
                }
            }
        }
    }

    public void manageState() {
        manageStateWithStartup(false);
    }

    public void manageStateWithStartup(boolean startup) {
        if (!startup && !isConnected() && this.audioPlayer != null)
            destroy(EndCase.DISCONNECTED);
        else {
            manageAlonePausing();
            if (settings.get(Setting.DISCONNECT_CLEANUP).toBoolean())
                manageQueue();
            manageVotes();
        }
    }

    public Controller perform(Consumer<Controller> controllerConsumer) {
        controllerConsumer.accept(this);
        return this;
    }

    public Controller perform(Runnable task) {
        task.run();
        return this;
    }

    public <R> R map(Function<Controller, R> controllerMapper) {
        return controllerMapper.apply(this);
    }

    public String getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Pulse.shardManager.getGuildById(guildId);
    }

    public Optional<TextChannel> getLastChannel() {
        return Optional.ofNullable(lastChannelId).flatMap(channel -> Optional.ofNullable(getGuild().getTextChannelById(channel)));
    }

    public String getLastChannelId() {
        return this.lastChannelId;
    }

    public Controller setLastChannelId(String lastChannelId) {
        this.lastChannelId = lastChannelId;
        return this;
    }

    public Controller updateLastChannelId(String lastChannelId, Supplier<Boolean> conditionSupplier) {
        if (conditionSupplier.get()) return setLastChannelId(lastChannelId);
        return this;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public Controller resetStatus(Status status) {
        this.lifetime = 0;
        this.status = status;
        return this;
    }

    public boolean incrementLifetimeWithStatusIfCurrentIsChangeable(Status newStatus) {
        if (status.isnt(Status.INACTIVE) && status.isnt(Status.ALONE) && status.isnt(Status.PAUSED)) //Testing if current status is changeable/resetable.
            resetStatus(newStatus);
        return setLifetime(lifetime + 1, newStatus).lifetime >= ControllerFactory.MAX_LIFETIME;
    }

    public Controller setLifetime(long lifetime, Status status) {
        if (this.status == null) this.status = status;
        if (this.status.equals(status)) this.lifetime = lifetime;
        return this;
    }

    public Settings getSettings() {
        return settings;
    }

    public AudioManager getAudioManager() {
        return getGuild().getAudioManager();
    }

    public User getSelfUser() {
        return getGuild().getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    public GuildVoiceState getVoiceState(Member member) {
        return member.getVoiceState();
    }

    public AudioChannel getAudioChannel(Member member) {
        GuildVoiceState voiceState = getVoiceState(member);
        if (voiceState == null || !voiceState.inAudioChannel()) return null;
        return voiceState.getChannel();
    }

    public List<Member> getConnectedMembers() {
        AudioManager audioManager = getAudioManager();
        List<Member> connectedMembers = new ArrayList<>(getConnectedChannel().map(IMemberContainer::getMembers).orElse(List.of()));
        connectedMembers.removeIf(member -> member.getUser().isBot());
        return connectedMembers;
    }

    public List<Member> getAllConnectedMembers() {
        AudioManager audioManager = getAudioManager();
        List<Member> connectedMembers = new ArrayList<>(getGuild().getChannels().stream()
                .filter(guildChannel -> guildChannel instanceof AudioChannel)
                .map(guildChannel -> (AudioChannel) guildChannel)
                .map(IMemberContainer::getMembers)
                .flatMap(Collection::stream)
                .toList());
        connectedMembers.removeIf(member -> member.getUser().isBot());
        return connectedMembers;
    }

    public Optional<AudioChannel> getConnectedChannel() {
        return Optional.ofNullable(getVoiceState(getSelfMember()).getChannel());
    }

    public boolean isConnected() {
        return getConnectedChannel().isPresent();
    }

    /**
     * @throws: IllegalArgumentException –
     * If the provided channel was null.
     * If the provided channel is not part of the Guild that the current audio connection is connected to.
     * UnsupportedOperationException – If audio is disabled due to an internal JDA error
     * net.dv8tion.jda.api.exceptions.InsufficientPermissionException –
     * If the currently logged in account does not have the Permission VOICE_CONNECT
     * If the currently logged in account does not have the Permission VOICE_MOVE_OTHERS and the user limit has been exceeded!
     */
    public Controller connect(Member member) throws IllegalArgumentException, InsufficientPermissionException {
        return connect(getAudioChannel(member));
    }

    /**
     * @throws: IllegalArgumentException –
     * If the provided channel was null.
     * If the provided channel is not part of the Guild that the current audio connection is connected to.
     * UnsupportedOperationException – If audio is disabled due to an internal JDA error
     * net.dv8tion.jda.api.exceptions.InsufficientPermissionException –
     * If the currently logged in account does not have the Permission VOICE_CONNECT
     * If the currently logged in account does not have the Permission VOICE_MOVE_OTHERS and the user limit has been exceeded!
     */
    public Controller connect(AudioChannel audioChannel) throws IllegalArgumentException, InsufficientPermissionException {
        logger.info("Connected to channel %s in guild %s.".formatted(audioChannel.getId(), audioChannel.getGuild().getId()));
        AudioManager audioManager = getAudioManager();
        audioManager.setSelfDeafened(false);
        audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
        if (audioPlayer == null) {
            audioPlayer = Pulse.audioPlayerManager.createPlayer();
            audioPlayer.addListener(new PlayerListener(this));
            audioPlayer.setVolume(this.volume = getSettings().get(Setting.DEFAULT_VOLUME).toInteger());
        }
        if (audioManager.getReceivingHandler() == null)
            audioManager.setReceivingHandler(this.audioReceiver = new AudioReceiver(this));
        if (audioManager.getSendingHandler() == null)
            audioManager.setSendingHandler(this.audioSender = new AudioSender(this));
        audioManager.openAudioConnection(audioChannel);
        return this;
    }

    /*
    General management.
     */

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public AudioReceiver getAudioReceiver() {
        return audioReceiver;
    }

    public AudioSender getAudioSender() {
        return audioSender;
    }

    public boolean wasAlone() {
        return this.alone;
    }

    public void setAlone(boolean alone) {
        this.alone = alone;
    }

    public void clearQueues() {
        this.queue.clear();
        this.previousQueue.clear();
    }

    public void setRemainingVotes(List<String> userIds) {
        this.votesToNext.removeIf(userId -> !userIds.contains(userId));
        this.votesToPrevious.removeIf(userId -> !userIds.contains(userId));
    }

    public void clearVotes() {
        this.votesToNext.clear();
        this.votesToPrevious.clear();
    }

    public void cancelTrack(boolean resetLastTrackId, boolean resetLastTrackIdIfNull) {
        if (audioPlayer != null) {
            audioPlayer.setPaused(false);
            audioPlayer.stopTrack();
            if (resetLastTrackId)
                setLastTrack(0);
            else
                setLastTrack(getPlayingTrack(), resetLastTrackIdIfNull);
        }
    }

    public void stop() {
        clearQueues();
        clearVotes();
        cancelTrack(true, true);
    }

    public void destroy(EndCase endCase) {
        boolean connected = isConnected() || endCase == EndCase.DISCONNECTED;
        stop();
        AudioManager audioManager = getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(this.audioSender = null);
        audioManager.setReceivingHandler(this.audioReceiver = null);
        if (this.audioPlayer != null) {
            this.audioPlayer.destroy();
            this.audioPlayer = null;
        }
        this.setRepeating(TriState.NONE);
        this.lastTrack = 0;
        this.alone = false;
        Pulse.eventManager.call(new PlayerDestroyedEvent(this, endCase, connected));
    }

    /*
    General player stuff.
     */

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isPaused() {
        return audioPlayer != null && audioPlayer.isPaused();
    }

    public void setPaused(boolean paused) {
        boolean changeState = audioPlayer.isPaused() != paused;
        if (!changeState) return;
        AudioTrack track = getPlayingTrack();
        if (track != null) {
            if (!track.isSeekable()) {
                changeState = false;
            }
        } else {
            paused = false;
        }
        if (changeState) audioPlayer.setPaused(paused);
    }

    public boolean isSeekable() {
        return this.getPlayingTrack() != null && this.getPlayingTrack().isSeekable();
    }

    public long getPosition() {
        if (isSeekable()) return this.getPlayingTrack().getPosition();
        return 0L;
    }

    public void setPosition(long position) {
        if (isSeekable()) this.getPlayingTrack().setPosition(position);
    }

    /*
    General queue stuff.
     */

    public int getLastTrack() {
        return this.lastTrack;
    }

    public void setLastTrack(int lastTrack) {
        this.lastTrack = lastTrack;
    }

    public void setLastTrack(AudioTrack audioTrack, boolean resetIfNull) {
        this.lastTrack = audioTrack == null ? (resetIfNull ? 0 : lastTrack) : AudioFactory.hashAudioObject(audioTrack);
    }

    public int getTotalQueueSize() {
        return previousQueue.size() + queue.size();
    }

    public Playlist getPreviousQueue() {
        return previousQueue;
    }

    public Playlist getQueue() {
        return queue;
    }

    public AudioTrack getPlayingTrack() {
        return audioPlayer != null ? audioPlayer.getPlayingTrack() : null;
    }

    public void restartTrack() {
        getPlayingTrack().setPosition(0);
    }

    public void restartQueue() {
        AudioTrack audioTrack = getPlayingTrack();
        if (audioTrack != null)
            queue.addFirst(audioTrack);
        queue.addAll(0, previousQueue);
        previousQueue.clear();
        nextTrack(true);
    }

    /**
     * @return False = Queue, None = Off, True = Track.
     */
    public TriState getRepeating() {
        return this.repeating;
    }

    public void setRepeating(TriState repeating) {
        this.repeating = repeating;
    }

    public void finishPlaying(boolean remove, boolean addToPrevious) {
        AudioTrack audioTrack = getPlayingTrack();
        if (audioTrack == null)
            return;
        cancelTrack(remove, false);
        if (!remove)
            if (addToPrevious)
                previousQueue.addLast(audioTrack);
            else
                queue.addFirst(audioTrack);
    }

    /*
     * asap
     *
     * TRUE -> Now
     * NONE -> First
     * FALSE -> Last
     */
    public int enqueue(AudioItem audioItem, TriState asap) {
        Playlist playlist = PlaylistFactory.from(AudioFactory.toPlaylist(audioItem));

        if (playlist.getTracks().isEmpty())
            return 0;

        if (playlist.getTracks().size() > 1 && playlist.getSelectedTrackIndex() > 0)
            playlist.setTracks(playlist.getSelectedTrack());

        if (settings.get(Setting.SHUFFLE_PLAYLISTS).toBoolean())
            Collections.shuffle(playlist.getTracks());

        int queueSize = queue.size();
        if (asap.isTrue()) {             // Start playing now! (finish track, add in other items to previous queue, play selected, add other items in future queue)
            // if there is a selected track,
            // finish playing current track.
            // add all items before selection to the previous queue,
            // then add the rest to the future queue.
            // then go to next track.
            finishPlaying(false, true);
            int selectedTrackIndex = playlist.getSelectedTrackIndex();
            if (selectedTrackIndex > 0) {
                for (int i = 0; i < selectedTrackIndex; i++)
                    previousQueue.addLast(playlist.remove(0));
            }
            queue.addAll(0, playlist);
            nextTrack();
            return playlist.size();
        } else if (asap.isNone()) {     // Start as next track. ()
            // If there is a selected track, limit the playlist to just the single track.
            // then add playlist to queue
            // then start if availible.
            if (playlist.getSelectedTrackIndex() > 0)  //add in single song only
                playlist.setTracks(playlist.getSelectedTrack());
            queue.addAll(0, playlist);
            if (getPlayingTrack() == null && queueSize == 0)
                nextTrack();
            return playlist.size();
        } else if (asap.isFalse()) {    // Start after queue is done.
            // Just add all tracks to end of queue.
            queue.addAll(playlist);
            if (getPlayingTrack() == null && queueSize == 0)
                nextTrack();
            return playlist.size();
        }
        return -1;
    }

    public boolean overridePlaying(AudioTrack audioTrack) {
        setLastTrack(getPlayingTrack(), false);
        return audioPlayer.startTrack(audioTrack.makeClone(), false);
    }

    private boolean startTrack(AudioTrack audioTrack, boolean force) {
        boolean started = audioPlayer.startTrack(audioTrack.makeClone(), !force); //retries handled with audio event listener track load exception handler.
        if (started)
            clearVotes();
        return started;
    }

    //called when track ends. reset last track if queue is looped.
    public void finished(AudioTrack audioTrack) {
        if (audioTrack != null) {
            if (repeating.isTrue())
                queue.addFirst(audioTrack);
            else
                previousQueue.addLast(audioTrack);
            setLastTrack(audioTrack, false);
        }
        nextTrack();
    }

    //Custom implementation because if next tracks cant resolve, then errors would occur if playlists on loop.
    public boolean skipTo(int item) {
        AudioTrack audioTrack = getPlayingTrack();

        finishPlaying(false, true);

        for (int i = 1; i <= item - 1; i++)
            previousQueue.addLast(queue.removeFirst());

        AudioTrack next = queue.pollFirst();
        if (next == null || !startTrack(next, true))
            return nextTrack();
        return true;
    }

    /**
     * @return True if playing another track, false if finished.
     */
    public boolean nextTrack() {
        return nextTrack(false);
    }

    /**
     * @return True if playing another track, false if finished.
     */
    public boolean nextTrack(boolean removeTrack) {
        finishPlaying(removeTrack, true);

        if (queue.isEmpty() && !previousQueue.isEmpty() && (repeating.isFalse() || settings.get(Setting.TWENTY_FOUR_SEVEN).toBoolean())) {
            queue.addAll(previousQueue);
            previousQueue.clear();
        }

        if (queue.isEmpty()) {
            Pulse.eventManager.call(new FinishedPlayingEvent(this));
            return false;
        }

        AudioTrack next = queue.pollFirst();
        if (next == null || !startTrack(next, true))
            return nextTrack();
        return true;
    }

    /**
     * @return True if playing another track, false if finished.
     */
    public boolean previousTrack() {
        return previousTrack(false);
    }

    /**
     * @return True if playing another track, false if finished.
     */
    public boolean previousTrack(boolean removeTrack) {
        finishPlaying(removeTrack, false);

        if (previousQueue.isEmpty() && !queue.isEmpty() && (repeating.isFalse() || settings.get(Setting.TWENTY_FOUR_SEVEN).toBoolean())) {
            previousQueue.addAll(queue);
            queue.clear();
        }

        if (previousQueue.isEmpty()) {
            Pulse.eventManager.call(new PreviousQueueReachedEndEvent(this));
            return false;
        }

        AudioTrack next = previousQueue.pollLast();
        if (next == null || !startTrack(next, true))
            return previousTrack();
        return true;
    }

    public void clearQueue() {
        queue.clear();
    }

    public void clearPreviousQueue() {
        previousQueue.clear();
    }

    public void recyclePrevious() {
        queue.addAll(previousQueue);
        previousQueue.clear();
    }

    public void shuffle() {
        Collections.shuffle(queue);
    }

    public void flip() {
        Collections.reverse(queue);
    }

    public void move(int index, int items, int to) {
        for (int i = 0; i < items; i++)
            queue.move(index, to + i); //if we dont add i, then it will move items in reverse order, where the last added item will be at the requested to index.
    }

    public void remove(int start, int items) {
        for (; items > 0; items--) {
            queue.remove(start);
        }
    }

    public boolean removeCurrent() {
        return nextTrack(true);
    }

    public void removeMemberRequests(String userId) {
        Iterator<AudioTrack> it = previousQueue.iterator();
        while (it.hasNext()) {
            UserData userData = UserDataFactory.from(it.next().getUserData());
            if (userData.requester().getId().equals(userId))
                it.remove();
        }
        it = queue.iterator();
        while (it.hasNext()) {
            UserData userData = UserDataFactory.from(it.next().getUserData());
            if (userData.requester().getId().equals(userId))
                it.remove();
        }
    }

    public void removeMemberRequestsExcept(List<String> userIds) {
        Iterator<AudioTrack> it = previousQueue.iterator();
        while (it.hasNext()) {
            UserData userData = UserDataFactory.from(it.next().getUserData());
            if (!userIds.contains(userData.requester().getId()))
                it.remove();
        }
        it = queue.iterator();
        while (it.hasNext()) {
            UserData userData = UserDataFactory.from(it.next().getUserData());
            if (!userIds.contains(userData.requester().getId()))
                it.remove();
        }
    }

    /*
     * General voting
     */

    public int neededVotes() {
        int connectedUsers = this.getConnectedMembers().size();
        return connectedUsers > 1 ? (connectedUsers / 2) : connectedUsers;
    }

    public int currentVotesToPrevious() {
        return votesToPrevious.size();
    }

    public int currentVotesToNext() {
        return votesToNext.size();
    }

    public boolean votePrevious(Member member) {
        String id = member.getUser().getId();
        if (votesToPrevious.contains(id)) {
            votesToPrevious.remove(id);
            return false;
        } else {
            votesToPrevious.add(id);
            return true;
        }
    }

    public boolean voteNext(Member member) {
        String id = member.getUser().getId();
        if (votesToNext.contains(id)) {
            votesToNext.remove(id);
            return false;
        } else {
            votesToNext.add(id);
            return true;
        }
    }

}
