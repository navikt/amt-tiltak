DO
$$
BEGIN
        if not exists
            (select 1 from pg_publication where pubname = 'amttiltak_publication')
        then
            CREATE PUBLICATION amttiltak_publication for ALL TABLES;
end if;
end;
$$;