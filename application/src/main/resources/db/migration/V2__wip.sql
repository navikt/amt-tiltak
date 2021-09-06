
CREATE TYPE tiltaksleverandor_rolle AS ENUM (
    'KOORDINATOR',
    'VEILEDER'
);

CREATE TABLE arena_arbgiverenhet (
    arbgiv_id SERIAL PRIMARY KEY,
    bedrnr int,
    orgnr_morselskap varchar,
    offnavn varchar,
    altnavn varchar,
    postnr varchar,
    postnr_sted varchar,
    reg_dato datetime,
    mod_dato datetime
);

CREATE TABLE xxxx (
    fodselsnr varchar,

    bedriftsnummer varchar,
    rolle tiltaksleverandor_rolle
);
