CREATE TABLE vurdering
(
    id                              uuid primary key,
    deltaker_id                     uuid                     not null references deltaker (id),
    opprettet_av_arrangor_ansatt_id uuid                     not null references arrangor_ansatt (id),
    vurderingstype                  varchar                  not null,
    begrunnelse                     varchar,
    gyldig_fra                      timestamp with time zone not null,
    gyldig_til                      timestamp with time zone
);