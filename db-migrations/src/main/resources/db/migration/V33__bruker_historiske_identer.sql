ALTER TABLE bruker
    ADD COLUMN historiske_identer text ARRAY default ARRAY[]::text[];