CREATE TABLE skjult_deltaker
(
    id                           uuid primary key,
    deltaker_id                  uuid                     not null references deltaker (id),
    skjult_av_arrangor_ansatt_id uuid                     not null references arrangor_ansatt (id),
    skjult_til                   timestamp with time zone not null default to_timestamp('3000-01-01','YYYY-MM-DD'),
    created_at                   timestamp with time zone not null default current_timestamp
);