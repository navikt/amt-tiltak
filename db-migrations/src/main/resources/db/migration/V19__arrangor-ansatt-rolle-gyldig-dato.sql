ALTER TABLE arrangor_ansatt_rolle ADD COLUMN gyldig_fra timestamp with time zone not null default current_timestamp;

ALTER TABLE arrangor_ansatt_rolle ADD COLUMN gyldig_til timestamp with time zone not null default TO_TIMESTAMP('3000-01-01','YYYY-MM-DD');
