ALTER TABLE arrangor_ansatt
    ADD COLUMN sist_velykkede_innlogging timestamp with time zone not null default 'epoch';
