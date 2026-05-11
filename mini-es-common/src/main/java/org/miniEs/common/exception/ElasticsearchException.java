package org.miniEs.common.exception;

/**
 * Base class for all mini-ES exceptions.
 * Mirrors org.elasticsearch.ElasticsearchException.
 */
public class ElasticsearchException extends RuntimeException {

    public ElasticsearchException(String message) {
        super(message);
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Returns the corresponding HTTP status code for this exception. */
    public int status() {
        return 500;
    }
}
