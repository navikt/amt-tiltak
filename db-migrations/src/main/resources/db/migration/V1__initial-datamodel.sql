CREATE TYPE tiltaksarrangor_rolle AS ENUM (
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

CREATE TABLE tiltaksarrangor
(
    id                                   uuid PRIMARY KEY,
    navn                                 varchar                  not null,
    organisasjonsnummer                  varchar                  not null UNIQUE,
    overordnet_enhet_navn                varchar,
    overordnet_enhet_organisasjonsnummer varchar,
    created_at                           timestamp with time zone not null default current_timestamp,
    modified_at                          timestamp with time zone not null default current_timestamp
);

CREATE TABLE tiltaksarrangor_ansatt
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

CREATE TABLE tiltaksarrangor_ansatt_rolle
(
    id                   uuid PRIMARY KEY,
    ansatt_id            uuid                     not null references tiltaksarrangor_ansatt (id),
    tiltaksarrangor_id uuid                     not null references tiltaksarrangor (id),
    rolle                tiltaksarrangor_rolle  not null,
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
    tiltaksarrangor_id uuid                     not null references tiltaksarrangor (id),
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
