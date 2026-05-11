package org.miniEs.common.exception;

/**
 * Thrown when a search phase fails.
 * Mirrors org.elasticsearch.action.search.SearchPhaseExecutionException.
 */
public class SearchPhaseExecutionException extends ElasticsearchException {

    public SearchPhaseExecutionException(String phaseName, String msg, Throwable cause) {
        super("Failed to execute phase [" + phaseName + "], " + msg, cause);
    }

    @Override
    public int status() {
        return 500;
    }
}
