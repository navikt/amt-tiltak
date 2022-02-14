INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');

INSERT INTO tiltak(id, navn, type)
VALUES ('b18fd670-3bc1-11ec-8d3d-0242ac130003', 'Tiltak1', 'AMO');

INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, registrert_dato,
                           fremmote_dato)
VALUES ('b3420940-5479-48c8-b2fa-3751c7a33aa2', 'b18fd670-3bc1-11ec-8d3d-0242ac130003',
        '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'Tiltaksgjennomforing1', 'GJENNOMFORES', '2022-02-01',
        '2050-12-30', '2022-01-01', '2022-02-01');

-- Nav ansatt og Nav kontor

INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
VALUES ('91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe', 'Z4321', 'Vashnir Veiledersen', '88776655', 'vashnir.veiledersen@nav.no');

INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
VALUES ('5e8790a9-7339-4ea2-ae75-54aac33f6c4d', 'Z1234', 'Ola Nordmann', '99887654', 'ola.nordmann@nav.no');


INSERT INTO nav_kontor (id, enhet_id, navn)
VALUES ('09405517-99c0-49e5-9eb3-31c61b9579cf', '1234', 'NAV Testheim');

-- Bruker 1

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
VALUES ('23b04c3a-a36c-451f-b9cf-30b6a6b586b8', '12345678910', 'Bruker 1 fornavn',
        'Bruker 1 etternavn', '73404782', 'bruker1@example.com', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe',
        '09405517-99c0-49e5-9eb3-31c61b9579cf');

INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling,
                      registrert_dato)
VALUES ('dc600c70-124f-4fe7-a687-b58439beb214', '23b04c3a-a36c-451f-b9cf-30b6a6b586b8',
        'b3420940-5479-48c8-b2fa-3751c7a33aa2', '2022-02-13', '2030-02-14', 5, 100,
        '2022-02-13');

INSERT INTO deltaker_status (id, deltaker_id, endret_dato, status, aktiv)
VALUES ('d6770809-29e3-47e0-8cc2-4fa667d1a756', 'dc600c70-124f-4fe7-a687-b58439beb214', '2022-02-13', 'DELTAR', TRUE);

-- Bruker 2

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
VALUES ('e05f8eb9-03d8-466a-8650-5788bdf5882e', '7908432423', 'Bruker 2 fornavn',
        'Bruker 2 etternavn', '65443532', 'bruker2@example.com', null,
        '09405517-99c0-49e5-9eb3-31c61b9579cf');

INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling,
                      registrert_dato)
VALUES ('daaac46f-c7af-4028-96c3-8fa81ec2b93c', 'e05f8eb9-03d8-466a-8650-5788bdf5882e',
        'b3420940-5479-48c8-b2fa-3751c7a33aa2', '2022-02-10', '2022-02-12', 3, 50,
        '2022-02-10');

INSERT INTO deltaker_status (id, deltaker_id, endret_dato, status, aktiv)
VALUES ('1264f224-7492-11ec-90d6-0242ac120003', 'daaac46f-c7af-4028-96c3-8fa81ec2b93c', '2022-02-11', 'DELTAR', TRUE);

-- Bruker 3

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
VALUES ('c74abcdd-53a7-4e22-86f3-97de9094029f', '3792473283', 'Bruker 3 fornavn',
        'Bruker 3 etternavn', '39057809', 'bruker3@example.com', null,
        '09405517-99c0-49e5-9eb3-31c61b9579cf');