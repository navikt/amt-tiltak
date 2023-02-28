CREATE TABLE arrangor_veileder (
  id                         uuid primary key,
  ansatt_id                  uuid                     not null references arrangor_ansatt (id),
  deltaker_id                uuid                     not null references deltaker (id),
  er_medveileder             boolean                  not null default false,
  gyldig_fra                 timestamp with time zone not null,
  gyldig_til                 timestamp with time zone not null,
  created_at                 timestamp with time zone not null default current_timestamp,
  modified_at                timestamp with time zone not null default current_timestamp
);