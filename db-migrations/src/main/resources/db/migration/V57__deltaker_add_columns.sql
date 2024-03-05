ALTER TABLE deltaker
    ADD COLUMN forste_vedtak_fattet date,
    ADD COLUMN historikk jsonb,
    ADD COLUMN sist_endret_av uuid,
    ADD COLUMN sist_endret_av_enhet uuid;