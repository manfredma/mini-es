package org.miniEs.common.unit;

import java.util.concurrent.TimeUnit;

/**
 * Represents a time value with unit.
 * Mirrors org.elasticsearch.common.unit.TimeValue.
 */
public class TimeValue {

    public static final TimeValue ZERO = new TimeValue(0, TimeUnit.MILLISECONDS);
    public static final TimeValue MINUS_ONE = new TimeValue(-1, TimeUnit.MILLISECONDS);

    private final long duration;
    private final TimeUnit timeUnit;

    public TimeValue(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public static TimeValue timeValueMillis(long millis) {
        return new TimeValue(millis, TimeUnit.MILLISECONDS);
    }

    public static TimeValue timeValueSeconds(long seconds) {
        return new TimeValue(seconds, TimeUnit.SECONDS);
    }

    public static TimeValue timeValueMinutes(long minutes) {
        return new TimeValue(minutes, TimeUnit.MINUTES);
    }

    public long millis() {
        return timeUnit.toMillis(duration);
    }

    public long seconds() {
        return timeUnit.toSeconds(duration);
    }

    public long nanos() {
        return timeUnit.toNanos(duration);
    }

    public static TimeValue parseTimeValue(String s) {
        if (s == null || s.isEmpty()) throw new IllegalArgumentException("time value cannot be empty");
        s = s.trim().toLowerCase();
        if (s.endsWith("ms")) {
            return timeValueMillis(Long.parseLong(s.substring(0, s.length() - 2)));
        } else if (s.endsWith("s")) {
            return timeValueSeconds(Long.parseLong(s.substring(0, s.length() - 1)));
        } else if (s.endsWith("m")) {
            return timeValueMinutes(Long.parseLong(s.substring(0, s.length() - 1)));
        } else if (s.endsWith("h")) {
            long hours = Long.parseLong(s.substring(0, s.length() - 1));
            return new TimeValue(hours, TimeUnit.HOURS);
        }
        throw new IllegalArgumentException("Unknown time value: " + s);
    }

    @Override
    public String toString() {
        if (duration < 0) return duration + "ms";
        long ms = millis();
        if (ms % 3600000 == 0) return (ms / 3600000) + "h";
        if (ms % 60000 == 0) return (ms / 60000) + "m";
        if (ms % 1000 == 0) return (ms / 1000) + "s";
        return ms + "ms";
    }
}
