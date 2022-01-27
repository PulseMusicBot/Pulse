package dev.westernpine.lib.object;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Timestamp {

    private TimeUnit timeUnit;
    private long duration;

    Timestamp() {
    }

    public Timestamp(TimeUnit timeUnit, long duration) {
        this.timeUnit = timeUnit;
        this.duration = duration;
    }

    /*
     * Instance
     */

    public static boolean isTimestamp(String timestamp) {
        if (timestamp.contains(":")) {
            return Stamp.isStamp(timestamp);
        } else {
            return timestamp.matches("[0-9]+");
        }
    }

    public static Timestamp from(String timestamp) {
        return from(TimeUnit.SECONDS, timestamp);
    }

    public static Timestamp from(TimeUnit defaultUnit, String timestamp) {
        Stamp stamp = Stamp.getMatching(timestamp);
        if (stamp != null)
            return stamp.toTimestamp(timestamp);
        if (timestamp.matches("[0-9]+"))
            return new Timestamp(defaultUnit, Long.parseLong(timestamp));
        return null;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Timestamp convert(TimeUnit timeUnit) {
        this.duration = timeUnit.convert(duration, this.timeUnit);
        this.timeUnit = timeUnit;
        return this;
    }

    public String toSmallFrameStamp() {
        Timestamp timestamp = this;
        if (!this.timeUnit.equals(TimeUnit.SECONDS))
            timestamp = timestamp.convert(TimeUnit.SECONDS);
        int seconds = (int) (duration % 60);
        int minutes = (int) ((duration / 60) % 60);
        int hours = (int) (((duration / 60) / 60) % 60);
        if (hours <= 0)
            return String.format("%02d:%02d", minutes, seconds);
        else
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static enum Stamp {
        HMS("^([0-9]{1,}):(([0-6]{1}[0]{1})|([0-5]{1}[0-9]{1})):(([0-6]{1}[0]{1})|([0-5]{1}[0-9]{1}))$") {
            @Override
            public Timestamp toTimestamp(String timestamp) {
                if (this.matches(timestamp)) {
                    String[] split = timestamp.split(":");
                    long hours = Long.parseLong(split[0]) * 60 * 60;
                    long minutes = Long.parseLong(split[1]) * 60;
                    long seconds = Long.parseLong(split[2]);
                    return new Timestamp(TimeUnit.SECONDS, hours + minutes + seconds);
                }
                return null;
            }
        },
        MS("^([0-9]{1,}):(([0-6]{1}[0]{1})|([0-5]{1}[0-9]{1}))$") {
            @Override
            public Timestamp toTimestamp(String timestamp) {
                if (this.matches(timestamp)) {
                    String[] split = timestamp.split(":");
                    long minutes = Long.parseLong(split[0]) * 60;
                    long seconds = Long.parseLong(split[1]);
                    return new Timestamp(TimeUnit.SECONDS, minutes + seconds);
                }
                return null;
            }
        },
        ;

        public static final String wildcardStringPattern = String.join("|", Arrays.asList(Stamp.values())
                .stream()
                .map(stamp -> stamp.getStringPattern())
                .collect(Collectors.toList()));
        private String stringPattern;

        Stamp(String stringPattern) {
            this.stringPattern = stringPattern;
        }

        public static boolean isStamp(String timestamp) {
            return Pattern.matches(wildcardStringPattern, timestamp);
        }

        public static Stamp getMatching(String timestamp) {
            for (Stamp stamp : Stamp.values())
                if (stamp.matches(timestamp))
                    return stamp;
            return null;
        }

        public String getStringPattern() {
            return stringPattern;
        }

        public boolean matches(String timestamp) {
            return Pattern.matches(wildcardStringPattern, timestamp) && Pattern.matches(stringPattern, timestamp);
        }

        public abstract Timestamp toTimestamp(String timestamp);
    }

}