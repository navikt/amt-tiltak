CREATE TYPE tiltaksleverandor_rolle AS ENUM (
    'KOORDINATOR',
    'VEILEDER'
);

CREATE TABLE nav_ansatt
(
    id              serial PRIMARY KEY,
    personlig_ident varchar,
    fornavn         varchar,
    etternavn       varchar,
    telefonnummer   varchar,
    epost           varchar
);

CREATE TABLE tiltaksleverandor
(
    id                  serial PRIMARY KEY,
    external_id         uuid UNIQUE,
    organisasjonsnummer varchar,
    organisasjonsnavn   varchar,
    virksomhetsnummer   varchar,
    virksomhetsnavn     varchar,
    created_at          timestamp with time zone default current_timestamp,
    modified_at         timestamp with time zone default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt
(
    id              serial PRIMARY KEY,
    external_id     uuid UNIQUE,
    personlig_ident varchar,
    fornavn         varchar,
    etternavn       varchar,
    telefonnummer   varchar,
    epost           varchar,
    created_at      timestamp with time zone default current_timestamp,
    modified_at     timestamp with time zone default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt_rolle
(
    id                   serial PRIMARY KEY,
    ansatt_id            integer references tiltaksleverandor_ansatt (id),
    tiltaksleverandor_id integer references tiltaksleverandor (id),
    rolle                tiltaksleverandor_rolle,
    created_at           timestamp with time zone default current_timestamp

);

CREATE TABLE tiltak
(
    id                   serial PRIMARY KEY,
    external_id          uuid UNIQUE,
    tiltaksleverandor_id integer references tiltaksleverandor (id),
    navn                 varchar,
    adresse              varchar,
    type                 varchar,
    created_at           timestamp with time zone default current_timestamp,
    modified_at          timestamp with time zone default current_timestamp
);

CREATE TABLE tiltaksinstans
(
    id             serial PRIMARY KEY,
    external_id    uuid UNIQUE,
    tiltak_id      integer references tiltak (id),
    navn           varchar,
    antall_plasser integer,
    created_at     timestamp with time zone default current_timestamp,
    modified_at    timestamp with time zone default current_timestamp
);

CREATE TABLE bruker
(
    id                    serial PRIMARY KEY,
    personlig_ident       varchar,
    fornavn               varchar,
    etternavn             varchar,
    telefonnummer         varchar,
    epost                 varchar,
    ansvarlig_veileder_id integer references nav_ansatt (id),
    created_at            timestamp with time zone default current_timestamp,
    modified_at           timestamp with time zone default current_timestamp
);

CREATE TABLE deltaker
(
    id                serial PRIMARY KEY,
    external_id       uuid UNIQUE,
    bruker_id         integer references bruker (id),
    tiltaksinstans_id integer references tiltaksinstans (id),
    status            varchar,
    created_at        timestamp with time zone default current_timestamp,
    modified_at       timestamp with time zone default current_timestamp
);

