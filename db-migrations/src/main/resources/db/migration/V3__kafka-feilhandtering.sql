CREATE SEQUENCE kafka_consumer_record_id_seq;

CREATE TABLE kafka_consumer_record
(
    id               BIGINT       NOT NULL PRIMARY KEY,
    topic            VARCHAR(100) NOT NULL,
    partition        INTEGER      NOT NULL,
    record_offset    BIGINT       NOT NULL,
    retries          INTEGER      NOT NULL DEFAULT 0,
    last_retry       TIMESTAMP,
    key              BYTEA,
    value            BYTEA,
    headers_json     TEXT,
    record_timestamp BIGINT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (topic, partition, record_offset)
);



