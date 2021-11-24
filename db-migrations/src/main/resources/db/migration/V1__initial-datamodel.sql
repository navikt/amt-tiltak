CREATE TYPE tiltaksleverandor_rolle AS ENUM (
    'KOORDINATOR',
    'VEILEDER'
    );

CREATE TABLE nav_ansatt
(
    id              uuid PRIMARY KEY,
    personlig_ident varchar NOT NULL UNIQUE,
    fornavn         varchar,
    etternavn       varchar,
    telefonnummer   varchar,
    epost           varchar
);

CREATE TABLE tiltaksleverandor
(
    id                                   uuid PRIMARY KEY,
    navn                                 varchar                  not null,
    organisasjonsnummer                  varchar                  not null UNIQUE,
    overordnet_enhet_navn                varchar,
    overordnet_enhet_organisasjonsnummer varchar,
    created_at                           timestamp with time zone not null default current_timestamp,
    modified_at                          timestamp with time zone not null default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt
(
    id              uuid PRIMARY KEY,
    personlig_ident varchar                  NOT NULL UNIQUE,
    fornavn         varchar,
    etternavn       varchar,
    telefonnummer   varchar,
    epost           varchar,
    created_at      timestamp with time zone not null default current_timestamp,
    modified_at     timestamp with time zone not null default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt_rolle
(
    id                   uuid PRIMARY KEY,
    ansatt_id            uuid                     not null references tiltaksleverandor_ansatt (id),
    tiltaksleverandor_id uuid                     not null references tiltaksleverandor (id),
    rolle                tiltaksleverandor_rolle  not null,
    created_at           timestamp with time zone not null default current_timestamp

);

CREATE TABLE tiltak
(
    id          uuid PRIMARY KEY,
    arena_id    varchar not null unique,
    navn        varchar,
    type        varchar,
    created_at  timestamp with time zone default current_timestamp,
    modified_at timestamp with time zone default current_timestamp
);

CREATE TYPE tiltaksinstans_status AS ENUM (
    'GJENNOMFORES', 'AVSLUTTET', 'IKKE_STARTET'
    );

CREATE TABLE tiltaksinstans
(
    id                   uuid PRIMARY KEY,
    arena_id             integer                  not null unique,
    tiltak_id            uuid                     not null references tiltak (id),
    tiltaksleverandor_id uuid                     not null references tiltaksleverandor (id),
    navn                 varchar,
    status               varchar,
    oppstart_dato        date,
    slutt_dato           date,
    registrert_dato      timestamp with time zone,
    fremmote_dato        timestamp with time zone,
    created_at           timestamp with time zone not null default current_timestamp,
    modified_at          timestamp with time zone not null default current_timestamp
);

CREATE TABLE bruker
(
    id                    uuid PRIMARY KEY,
    fodselsnummer         varchar not null UNIQUE,
    fornavn               varchar not null,
    mellomnavn            varchar,
    etternavn             varchar not null,
    telefonnummer         varchar,
    epost                 varchar,
    ansvarlig_veileder_id uuid references nav_ansatt (id),
    created_at            timestamp with time zone default current_timestamp,
    modified_at           timestamp with time zone default current_timestamp
);

CREATE TABLE deltaker
(
    id                uuid PRIMARY KEY,
    bruker_id         uuid not null references bruker (id),
    tiltaksinstans_id uuid not null references tiltaksinstans (id),
    oppstart_dato     date,
    slutt_dato        date,
    status            varchar,
    created_at        timestamp with time zone default current_timestamp,
    modified_at       timestamp with time zone default current_timestamp
);
