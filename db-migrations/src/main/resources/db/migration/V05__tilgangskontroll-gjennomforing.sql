CREATE TABLE arrangor_ansatt_gjennomforing_tilgang
(
    id               uuid PRIMARY KEY,
    ansatt_id        uuid                     not null references arrangor_ansatt (id),
    gjennomforing_id uuid                     not null references gjennomforing (id),
    created_at       timestamp with time zone not null default current_timestamp
);