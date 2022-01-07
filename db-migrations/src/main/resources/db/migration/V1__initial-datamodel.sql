CREATE TYPE arrangor_rolle AS ENUM (
    'KOORDINATOR',
    'VEILEDER'
    );

CREATE TABLE nav_ansatt
(
    id              uuid PRIMARY KEY,
    personlig_ident varchar NOT NULL UNIQUE,
    telefonnummer   varchar,
    epost           varchar,
    navn            VARCHAR NOT NULL
);

CREATE TABLE nav_kontor
(
    id       uuid PRIMARY KEY NOT NULL,
    enhet_id varchar UNIQUE   NOT NULL,
    navn     varchar          NOT NULL
);

CREATE TABLE arrangor
(
    id                                   uuid PRIMARY KEY,
    navn                                 varchar                  not null,
    organisasjonsnummer                  varchar                  not null UNIQUE,
    overordnet_enhet_navn                varchar,
    overordnet_enhet_organisasjonsnummer varchar,
    created_at                           timestamp with time zone not null default current_timestamp,
    modified_at                          timestamp with time zone not null default current_timestamp
);

CREATE TABLE arrangor_ansatt
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

CREATE TABLE arrangor_ansatt_rolle
(
    id          uuid PRIMARY KEY,
    ansatt_id   uuid                     not null references arrangor_ansatt (id),
    arrangor_id uuid                     not null references arrangor (id),
    rolle       arrangor_rolle           not null,
    created_at  timestamp with time zone not null default current_timestamp

);

CREATE TABLE tiltak
(
    id          uuid PRIMARY KEY,
    navn        varchar,
    type        varchar,
    created_at  timestamp with time zone default current_timestamp,
    modified_at timestamp with time zone default current_timestamp
);

CREATE TYPE gjennomforing_status AS ENUM (
    'GJENNOMFORES', 'AVSLUTTET', 'IKKE_STARTET'
    );

CREATE TABLE gjennomforing
(
    id              uuid PRIMARY KEY,
    tiltak_id       uuid                     not null references tiltak (id),
    arrangor_id     uuid                     not null references arrangor (id),
    navn            varchar,
    status          varchar,
    oppstart_dato   date,
    slutt_dato      date,
    registrert_dato timestamp with time zone,
    fremmote_dato   timestamp with time zone,
    created_at      timestamp with time zone not null default current_timestamp,
    modified_at     timestamp with time zone not null default current_timestamp
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
    nav_kontor_id         uuid references nav_kontor (id),
    created_at            timestamp with time zone default current_timestamp,
    modified_at           timestamp with time zone default current_timestamp
);

CREATE TABLE deltaker
(
    id               uuid PRIMARY KEY,
    bruker_id        uuid                     not null references bruker (id),
    gjennomforing_id uuid                     not null references gjennomforing (id),
    oppstart_dato    date,
    slutt_dato       date,
    status           varchar,
    created_at       timestamp with time zone default current_timestamp,
    modified_at      timestamp with time zone default current_timestamp,
    dager_per_uke    integer,
    prosent_stilling float,
    registrert_dato  timestamp with time zone not null
);
