-- Gir tilgang til alle nye tabeller og sekvenser i 'public' schema til cloudsqliamuser hvis rollen finnes

create or replace procedure grant_all_cloudsqliamuser()
    language plpgsql
as
$$
declare
begin
    IF (SELECT exists(SELECT rolname FROM pg_roles WHERE rolname = 'cloudsqliamuser')) THEN
        GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cloudsqliamuser;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cloudsqliamuser;
    END IF;
end;
$$;

call grant_all_cloudsqliamuser();

drop procedure grant_all_cloudsqliamuser;