alter table tiltaksinstans
    drop column antall_plasser;

alter table tiltaksinstans
    add column arena_id integer unique;

alter table tiltaksinstans
    add column oppstart_dato timestamp;

alter table tiltaksinstans
    add column slutt_dato timestamp;

alter table tiltaksinstans
    add column registrert_dato timestamp;

alter table tiltaksinstans
    add column fremmote_dato timestamp;

CREATE TYPE tiltaksinstans_status AS ENUM (
    'GJENNOMFORES', 'AVSLUTTET', 'IKKE_STARTET'
    );

alter table tiltaksinstans
    add column status tiltaksinstans_status
