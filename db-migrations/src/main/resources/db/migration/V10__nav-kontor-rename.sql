
ALTER TABLE nav_kontor
    RENAME TO nav_enhet;

ALTER TABLE bruker
    RENAME COLUMN nav_kontor_id TO nav_enhet_id;

ALTER TABLE gjennomforing
    RENAME COLUMN nav_kontor_id TO nav_enhet_id;