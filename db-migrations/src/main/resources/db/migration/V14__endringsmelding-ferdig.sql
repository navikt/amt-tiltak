ALTER TABLE endringsmelding
    ADD COLUMN ferdiggjort_tidspunkt timestamp with time zone;

ALTER TABLE endringsmelding
    RENAME COLUMN godkjent_av_nav_ansatt TO ferdiggjort_av_nav_ansatt_id;

ALTER TABLE endringsmelding
    RENAME COLUMN opprettet_av TO opprettet_av_arrangor_ansatt_id;
