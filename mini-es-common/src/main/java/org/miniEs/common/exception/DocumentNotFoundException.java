package org.miniEs.common.exception;

/**
 * Thrown when a document does not exist (used internally; get returns found=false rather than throwing).
 * Mirrors org.elasticsearch.index.engine.DocumentMissingException.
 */
public class DocumentNotFoundException extends ElasticsearchException {

    public DocumentNotFoundException(String index, String id) {
        super("[" + index + "] document missing for id [" + id + "]");
    }

    @Override
    public int status() {
        return 404;
    }
}
