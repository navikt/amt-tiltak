
CREATE TYPE tiltaksleverandor_rolle AS ENUM (
    'KOORDINATOR',
    'VEILEDER'
);

CREATE TABLE xxxx (
    fodselsnr varchar,
    bedriftsnummer varchar,
    rolle tiltaksleverandor_rolle
);
