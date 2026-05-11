package org.miniEs.common.xcontent;

/**
 * Supported content types. mini-ES only implements JSON.
 * Mirrors org.elasticsearch.common.xcontent.XContentType.
 */
public enum XContentType {

    JSON("application/json");

    private final String mediaType;

    XContentType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String mediaType() {
        return mediaType;
    }

    public static XContentType fromMediaType(String mediaType) {
        if (mediaType != null && mediaType.contains("application/json")) {
            return JSON;
        }
        return JSON; // default
    }
}
