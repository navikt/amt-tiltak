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
    external_id         uuid                     not null UNIQUE,
    organisasjonsnummer varchar                  not null,
    organisasjonsnavn   varchar                  not null,
    virksomhetsnummer   varchar                  not null,
    virksomhetsnavn     varchar                  not null,
    created_at          timestamp with time zone not null default current_timestamp,
    modified_at         timestamp with time zone not null default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt
(
    id              serial PRIMARY KEY,
    external_id     uuid                     not null UNIQUE,
    personlig_ident varchar,
    fornavn         varchar,
    etternavn       varchar,
    telefonnummer   varchar,
    epost           varchar,
    created_at      timestamp with time zone not null default current_timestamp,
    modified_at     timestamp with time zone not null default current_timestamp
);

CREATE TABLE tiltaksleverandor_ansatt_rolle
(
    id                   serial PRIMARY KEY,
    ansatt_id            integer                  not null references tiltaksleverandor_ansatt (id),
    tiltaksleverandor_id integer                  not null references tiltaksleverandor (id),
    rolle                tiltaksleverandor_rolle  not null,
    created_at           timestamp with time zone not null default current_timestamp

);

CREATE TABLE tiltak
(
    id                   serial PRIMARY KEY,
    external_id          uuid    not null UNIQUE,
    arena_id             varchar not null unique,
    tiltaksleverandor_id integer not null references tiltaksleverandor (id),
    navn                 varchar,
    adresse              varchar,
    type                 varchar,
    created_at           timestamp with time zone default current_timestamp,
    modified_at          timestamp with time zone default current_timestamp
);

CREATE TABLE tiltaksinstans
(
    id             serial PRIMARY KEY,
    external_id    uuid                     not null UNIQUE,
    tiltak_id      integer                  not null references tiltak (id),
    navn           varchar,
    antall_plasser integer,
    created_at     timestamp with time zone not null default current_timestamp,
    modified_at    timestamp with time zone not null default current_timestamp
);

CREATE TABLE bruker
(
    id                    serial not null PRIMARY KEY,
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
    id                serial  not null PRIMARY KEY,
    external_id       uuid    not null UNIQUE,
    bruker_id         integer not null references bruker (id),
    tiltaksinstans_id integer not null references tiltaksinstans (id),
    status            varchar,
    created_at        timestamp with time zone default current_timestamp,
    modified_at       timestamp with time zone default current_timestamp
);

