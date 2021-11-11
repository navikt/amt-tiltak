CREATE TABLE arena_tiltak_ids_ignored
(
    tiltak_id BIGINT PRIMARY KEY NOT NULL,
)

ALTER TYPE arena_ingest_status ADD VALUE ('IGNORED');