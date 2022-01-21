package dev.westernpine.pulse.audio.track.userdata.requester;

public class Requester {

    String id, name, discriminator, avatarUrl;

    Requester(String id, String name, String discriminator, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.discriminator = discriminator;
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getMention() {
        return "<@" + id + ">";
    }

    public boolean is(Requester requester) {
        return requester.id.equals(id);
    }
}
