DROP TABLE IF EXISTS endringsmelding;

CREATE TABLE endringsmelding
(
    id                              uuid PRIMARY KEY,
    deltaker_id                     uuid                     not null references deltaker (id),
    utfort_av_nav_ansatt_id         uuid references nav_ansatt (id),
    opprettet_av_arrangor_ansatt_id uuid                     not null references arrangor_ansatt (id),
    utfort_tidspunkt                timestamp with time zone,
    status                          varchar                  not null,
    type                            varchar                  not null,
    innhold                         jsonb                    not null,
    created_at                      timestamp with time zone not null default current_timestamp,
    modified_at                     timestamp with time zone not null default current_timestamp
);