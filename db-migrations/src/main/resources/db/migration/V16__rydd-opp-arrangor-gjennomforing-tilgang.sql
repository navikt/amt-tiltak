
ALTER TABLE arrangor_ansatt_gjennomforing_tilgang DROP COLUMN opprettet_av_nav_ansatt_id;

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang DROP COLUMN stoppet_av_nav_ansatt_id;

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang DROP COLUMN stoppet_tidspunkt;

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang ADD COLUMN gyldig_fra timestamp with time zone not null default current_timestamp;

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang ADD COLUMN gyldig_til timestamp with time zone not null default TO_TIMESTAMP('3000-01-01','YYYY-MM-DD');
