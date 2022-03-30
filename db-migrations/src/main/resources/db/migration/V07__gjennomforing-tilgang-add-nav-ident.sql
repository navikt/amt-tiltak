ALTER TABLE arrangor_ansatt_gjennomforing_tilgang
    ADD COLUMN opprettet_av_nav_ident varchar not null default '';

ALTER TABLE arrangor_ansatt_gjennomforing_tilgang
    ADD COLUMN utlopt_dato timestamp with time zone;
