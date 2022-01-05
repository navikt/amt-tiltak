CREATE TABLE nav_kontor
(
    id       uuid PRIMARY KEY NOT NULL,
    enhet_id varchar UNIQUE   NOT NULL,
    navn     varchar          NOT NULL
);

ALTER TABLE bruker
    ADD COLUMN nav_kontor_id uuid references nav_kontor (id);

