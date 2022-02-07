INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id)
VALUES ('23b04c3a-a36c-451f-b9cf-30b6a6b586b8', '12345678910', 'Bruker Fornavn', 'Bruker Etternavn', '384', 'm@2.c', null);

INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');

INSERT INTO tiltak(id, navn, type)
VALUES ('b18fd670-3bc1-11ec-8d3d-0242ac130003', 'Tiltak1', 'AMO');

INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, registrert_dato, fremmote_dato)
VALUES ('b3420940-5479-48c8-b2fa-3751c7a33aa2', 'b18fd670-3bc1-11ec-8d3d-0242ac130003', '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'Tiltaksgjennomforing1', 'GJENNOMFORES', current_date,
        current_date, current_timestamp, current_timestamp);

INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling, registrert_dato)
VALUES ('dc600c70-124f-4fe7-a687-b58439beb214', '23b04c3a-a36c-451f-b9cf-30b6a6b586b8', 'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE , 'tomorrow'::DATE, 5, 100, 'yesterday'::DATE);

INSERT INTO deltaker_status (id, deltaker_id, endret_dato, status, aktiv)
VALUES ('1264f224-7492-11ec-90d6-0242ac120003', 'dc600c70-124f-4fe7-a687-b58439beb214', 'yesterday'::DATE , 'DELTAR', TRUE);
