package org.miniEs.common.exception;

/**
 * Thrown when an optimistic concurrency check (version conflict) fails.
 * Mirrors org.elasticsearch.index.engine.VersionConflictEngineException.
 */
public class VersionConflictEngineException extends ElasticsearchException {

    public VersionConflictEngineException(String index, String id, long currentVersion, long expectedVersion) {
        super("[" + index + "]: version conflict, current version [" + currentVersion
                + "] is different than the one provided [" + expectedVersion + "] for document id [" + id + "]");
    }

    @Override
    public int status() {
        return 409;
    }
}
