DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'amt-tiltak-v1')
        THEN
            ALTER USER "amt-tiltak-v1" WITH REPLICATION;
END IF;
END
$$;
DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'datastream')
        THEN
            ALTER USER "datastream" WITH REPLICATION;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "datastream";
            GRANT USAGE ON SCHEMA public TO "datastream";
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO "datastream";
END IF;
END
$$;