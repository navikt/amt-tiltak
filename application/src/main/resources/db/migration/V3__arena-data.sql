CREATE TYPE arena_operation_type AS ENUM (
    'INSERT',
    'UPDATE',
    'DELETE'
    );

CREATE TYPE arena_ingest_status AS ENUM (
    'NEW',
    'INGESTED',
    'RETRY',
    'FAILED'
    );

CREATE TABLE arena_data
(
    id                  SERIAL PRIMARY KEY   NOT NULL,
    table_name          VARCHAR              NOT NULL,
    operation_type      arena_operation_type NOT NULL,
    operation_pos       BIGINT               NOT NULL,
    operation_timestamp TIMESTAMP            NOT NULL,
    ingest_status       arena_ingest_status  NOT NULL DEFAULT 'NEW',
    ingested_timestamp  TIMESTAMP,
    ingest_attempts     INT                  NOT NULL DEFAULT 0,
    last_retry          TIMESTAMP,
    before              JSON,
    after               JSON
)
