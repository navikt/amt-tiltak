INSERT INTO nav_ansatt (id, personlig_ident, navn, telefonnummer, epost)
VALUES ('91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe', '1234', 'Vashnir Veiledersen', '84756', 'vashnir.veiledersen@nav.no');

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id)
VALUES ('23b04c3a-a36c-451f-b9cf-30b6a6b586b8', '12345678910', 'Bruker Fornavn', 'Bruker Etternavn', '384', 'm@2.c', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe');

INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');

INSERT INTO tiltak(id, arena_id, navn, type)
VALUES ('b18fd670-3bc1-11ec-8d3d-0242ac130003', '1', 'Tiltak1', 'AMO');

INSERT INTO tiltaksinstans (id, arena_id, tiltak_id, arrangor_id, navn, status, oppstart_dato, slutt_dato, registrert_dato, fremmote_dato)
VALUES ('b3420940-5479-48c8-b2fa-3751c7a33aa2', 1, 'b18fd670-3bc1-11ec-8d3d-0242ac130003', '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'Tiltaksinstans1', 'GJENNOMFORES', current_date,
        current_date, current_timestamp, current_timestamp);

INSERT INTO deltaker (id, bruker_id, tiltaksinstans_id, oppstart_dato, slutt_dato, status, arena_status, dager_per_uke, prosent_stilling)
VALUES ('dc600c70-124f-4fe7-a687-b58439beb214', '23b04c3a-a36c-451f-b9cf-30b6a6b586b8', 'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE , 'tomorrow'::DATE, 'GJENNOMFORES', 'TILBUD', 5, 100);
