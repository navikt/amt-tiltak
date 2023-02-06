ALTER TABLE bruker
    ADD COLUMN identer text ARRAY default ARRAY[]::text[];