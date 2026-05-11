package org.miniEs.common.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable key-value configuration store.
 * Mirrors org.elasticsearch.common.settings.Settings.
 */
public class Settings {

    public static final Settings EMPTY = new Settings(Collections.emptyMap());

    private final Map<String, String> settings;

    private Settings(Map<String, String> settings) {
        this.settings = Collections.unmodifiableMap(new HashMap<>(settings));
    }

    public String get(String key) {
        return settings.get(key);
    }

    public String get(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public int getAsInt(String key, int defaultValue) {
        String value = settings.get(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public long getAsLong(String key, long defaultValue) {
        String value = settings.get(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }

    public boolean getAsBoolean(String key, boolean defaultValue) {
        String value = settings.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public boolean hasValue(String key) {
        return settings.containsKey(key);
    }

    public Map<String, String> getAsMap() {
        return settings;
    }

    public Settings merge(Settings other) {
        Map<String, String> merged = new HashMap<>(this.settings);
        merged.putAll(other.settings);
        return new Settings(merged);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> map = new HashMap<>();

        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Builder put(String key, int value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, long value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, boolean value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(Settings settings) {
            map.putAll(settings.settings);
            return this;
        }

        public Settings build() {
            return new Settings(map);
        }
    }

    @Override
    public String toString() {
        return settings.toString();
    }
}
