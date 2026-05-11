package org.miniEs.common.exception;

/**
 * Thrown when an operation targets an index that does not exist.
 * Mirrors org.elasticsearch.index.IndexNotFoundException.
 */
public class IndexNotFoundException extends ElasticsearchException {

    private final String index;

    public IndexNotFoundException(String index) {
        super("no such index [" + index + "]");
        this.index = index;
    }

    public String index() {
        return index;
    }

    @Override
    public int status() {
        return 404;
    }
}
