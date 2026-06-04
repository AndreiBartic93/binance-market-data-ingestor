package org.ingestor.entity.enums;

/**
 * Status of a concrete ingestion execution.
 */
public enum IngestionRunStatus {
    STARTED,
    SUCCESS,
    FAILED,
    PARTIAL,
    STOPPED
}