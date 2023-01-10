ALTER TABLE arrangor_ansatt
    ADD COLUMN tilganger_sist_synkronisert timestamp with time zone not null default 'epoch';
