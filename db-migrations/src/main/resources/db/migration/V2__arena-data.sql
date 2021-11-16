CREATE TABLE arena_data
(
    id                  SERIAL PRIMARY KEY NOT NULL,
    table_name          VARCHAR            NOT NULL,
    operation_type      VARCHAR            NOT NULL CHECK ( operation_type IN ('INSERT', 'UPDATE', 'DELETE')),
    operation_pos       BIGINT             NOT NULL,
    operation_timestamp TIMESTAMP          NOT NULL,
    ingest_status       VARCHAR            NOT NULL DEFAULT 'NEW' CHECK ( ingest_status IN ('NEW', 'INGESTED', 'RETRY', 'FAILED', 'IGNORED')),
    ingested_timestamp  TIMESTAMP,
    ingest_attempts     INT                NOT NULL DEFAULT 0,
    last_retry          TIMESTAMP,
    before              JSONB,
    after               JSONB
);

CREATE UNIQUE INDEX arena_data_table_operation_type_operation_pos_idx on arena_data (table_name, operation_type, operation_pos);

CREATE TABLE arena_tiltak_ids_ignored
(
    tiltak_id BIGINT PRIMARY KEY NOT NULL
);
