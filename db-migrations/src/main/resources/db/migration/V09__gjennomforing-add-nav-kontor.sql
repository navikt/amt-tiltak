ALTER TABLE gjennomforing
    ADD COLUMN nav_kontor_id UUID REFERENCES nav_kontor(id);