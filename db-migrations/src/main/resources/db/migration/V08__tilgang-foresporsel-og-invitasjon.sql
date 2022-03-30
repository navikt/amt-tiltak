CREATE TABLE gjennomforing_tilgang_foresporsel
(
    id                          uuid primary key,
    personlig_ident             varchar                  not null,
    fornavn                     varchar                  not null,
    mellomnavn                  varchar,
    etternavn                   varchar                  not null,
    gjennomforing_id            uuid                     not null references gjennomforing (id),
    beslutning_av_nav_ansatt_id uuid references nav_ansatt (id),
    tidspunkt_beslutning        timestamp with time zone,
    beslutning                  varchar check (beslutning in ('GODKJENT', 'AVVIST')),
    tilgang_foresporsel_id      uuid references arrangor_ansatt_rolle (id),
    created_at                  timestamp with time zone not null default current_timestamp
);

CREATE TABLE gjennomforing_tilgang_invitasjon
(
    id                         uuid primary key,
    gjennomforing_id           uuid                     not null references gjennomforing (id),
    gydlig_til                 timestamp with time zone not null,
    opprettet_av_nav_ansatt_id uuid                     not null references nav_ansatt (id),
    er_brukt                   boolean                  not null default false,
    tidspunkt_brukt            timestamp with time zone not null,
    tilgang_foresporsel_id     uuid references gjennomforing_tilgang_foresporsel (id),
    created_at                 timestamp with time zone not null default current_timestamp
);
