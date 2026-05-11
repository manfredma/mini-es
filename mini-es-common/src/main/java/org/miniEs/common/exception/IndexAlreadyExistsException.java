package org.miniEs.common.exception;

/**
 * Thrown when creating an index that already exists.
 * Mirrors org.elasticsearch.ResourceAlreadyExistsException.
 */
public class IndexAlreadyExistsException extends ElasticsearchException {

    public IndexAlreadyExistsException(String index) {
        super("index [" + index + "] already exists");
    }

    @Override
    public int status() {
        return 400;
    }
}
