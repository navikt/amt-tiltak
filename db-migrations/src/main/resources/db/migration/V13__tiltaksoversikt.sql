CREATE TABLE tiltaksansavarlig_gjennomforing_tilgang
(
    id               uuid primary key,
    nav_ansatt_id    uuid                     not null references nav_ansatt (id),
    gjennomforing_id uuid                     not null references gjennomforing (id),
    gyldig_til       timestamp with time zone not null,
    created_at       timestamp with time zone not null default current_timestamp
);