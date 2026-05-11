package org.miniEs.common.exception;

/**
 * Base class for transport-related exceptions.
 * Mirrors org.elasticsearch.transport.TransportException.
 */
public class TransportException extends ElasticsearchException {

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int status() {
        return 503;
    }
}
