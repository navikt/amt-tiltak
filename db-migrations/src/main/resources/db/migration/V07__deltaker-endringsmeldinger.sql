CREATE TABLE endringsmelding
(
    id                          uuid PRIMARY KEY,
    deltaker_id                 uuid not null references deltaker (id),
    start_dato                  date,
    godkjent_av_nav_ansatt      uuid references nav_ansatt(id),
    aktiv                       boolean not null,
    opprettet_av                uuid not null references arrangor_ansatt(id),
    created_at                  timestamp with time zone not null default current_timestamp,
    modified_at                 timestamp with time zone not null default current_timestamp
);