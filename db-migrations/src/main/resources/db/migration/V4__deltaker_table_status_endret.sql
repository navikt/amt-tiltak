ALTER TABLE deltaker DROP COLUMN status;

CREATE TABLE deltaker_status
(
    id          uuid PRIMARY KEY NOT NULL,
    deltaker_id uuid references deltaker (id),
    endret_dato date,
    status      varchar          NOT NULL,
    active      boolean,

    UNIQUE (deltaker_id, endret_dato, status)
);

CREATE INDEX deltaker_status_deltaker_id_idx ON deltaker_status(deltaker_id);
