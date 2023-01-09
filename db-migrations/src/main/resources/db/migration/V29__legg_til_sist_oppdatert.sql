ALTER TABLE arrangor_ansatt
    ADD COLUMN sist_oppdatert timestamp with time zone not null default 'epoch';
