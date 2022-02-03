INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
VALUES ('91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe', '1234', 'Vashnir Veiledersen', '84756', 'vashnir.veiledersen@nav.no');

INSERT INTO nav_kontor (id, enhet_id, navn)
VALUES ('09405517-99c0-49e5-9eb3-31c61b9579cf', '1234', 'NAV Testheim');

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
VALUES ('23b04c3a-a36c-451f-b9cf-30b6a6b586b8', '12345678910', 'Bruker Fornavn',
        'Bruker Etternavn', '384', 'm@2.c', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe',
        '09405517-99c0-49e5-9eb3-31c61b9579cf');

INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');

INSERT INTO tiltak(id, navn, type)
VALUES ('b18fd670-3bc1-11ec-8d3d-0242ac130003', 'Tiltak1', 'AMO');

INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, registrert_dato,
                           fremmote_dato)
VALUES ('b3420940-5479-48c8-b2fa-3751c7a33aa2', 'b18fd670-3bc1-11ec-8d3d-0242ac130003',
        '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'Tiltaksgjennomforing1', 'GJENNOMFORES', current_date,
        current_date, current_timestamp, current_timestamp);

INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling,
                      registrert_dato)
VALUES ('dc600c70-124f-4fe7-a687-b58439beb214', '23b04c3a-a36c-451f-b9cf-30b6a6b586b8',
        'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE, 'tomorrow'::DATE, 5, 100,
        'yesterday'::DATE);
