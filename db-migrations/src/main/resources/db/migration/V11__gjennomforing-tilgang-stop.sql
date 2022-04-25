-- opprettet_av_nav_ansatt_id settes til not null i senere script etter migrering
ALTER TABLE arrangor_ansatt_gjennomforing_tilgang
    ADD COLUMN opprettet_av_nav_ansatt_id UUID REFERENCES nav_ansatt(id);

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang
    ADD COLUMN stoppet_av_nav_ansatt_id UUID REFERENCES nav_ansatt(id);

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang
    ADD COLUMN stoppet_tidspunkt timestamp with time zone;