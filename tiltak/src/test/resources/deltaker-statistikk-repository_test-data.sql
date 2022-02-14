DO $$
DECLARE
    brukerId1 uuid := '23b04c3a-a36c-451f-b9cf-30b6a6b586b8';
    deltaker1 uuid := 'dc600c70-124f-4fe7-a687-b58439beb214';

    brukerId2 uuid := '82536415-da79-4654-91c6-b68ddf68da16';
    deltaker2 uuid := '25b3ef51-02ff-4f26-8f1b-1ad9215458d3';

    brukerId3 uuid := 'cd8930fe-b29b-4a7d-ae90-d1fd9e462f06';
    deltaker3 uuid := '8b822579-a7af-4f6a-91af-416c08e29a62';

    arrangor1 uuid := '8a37bce6-3bc1-11ec-8d3d-0242ac130003';
    arrangor2 uuid := 'e5f0725d-d0f9-4660-8e53-64e00ceeb7c9';
    ansatt1 uuid := '90f5bda4-28b6-41ba-9385-86d5e0e27103';

BEGIN
INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
VALUES ('91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe', '1234', 'Vashnir Veiledersen', '84756', 'vashnir.veiledersen@nav.no');

INSERT INTO nav_kontor (id, enhet_id, navn)
VALUES ('09405517-99c0-49e5-9eb3-31c61b9579cf', '1234', 'NAV Testheim');

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
VALUES
       (brukerId1, '12345678910', 'Bruker Fornavn',
        'Bruker Etternavn', '384', 'm@2.c', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe',
        '09405517-99c0-49e5-9eb3-31c61b9579cf'),
       (brukerId2, '12345678911', 'Bruker Fornavn',
        'Bruker Etternavn', '384', 'm@2.c', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe',
        '09405517-99c0-49e5-9eb3-31c61b9579cf'),
       (brukerId3, '12345678912', 'Bruker Fornavn',
        'Bruker Etternavn', '384', 'm@2.c', '91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe',
        '09405517-99c0-49e5-9eb3-31c61b9579cf');

INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES (arrangor1, '1', 'OrgNavn', '2', 'VirkNavn'),
       (arrangor2, '3', 'OrgNavn', '4', 'VirkNavn2');

INSERT INTO arrangor_ansatt (id, personlig_ident, fornavn, mellomnavn, etternavn)
VALUES (ansatt1, '11112222333', 'fnavn', 'mnavn', 'enavn');

INSERT INTO arrangor_ansatt_rolle (id, ansatt_id, arrangor_id, rolle)
VALUES ('3843d278-673c-4aea-8441-38db7b71be7d', ansatt1, arrangor1, 'KOORDINATOR');

INSERT INTO tiltak(id, navn, type)
VALUES ('b18fd670-3bc1-11ec-8d3d-0242ac130003', 'Tiltak1', 'AMO');

INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, registrert_dato,
                           fremmote_dato)
VALUES ('b3420940-5479-48c8-b2fa-3751c7a33aa2', 'b18fd670-3bc1-11ec-8d3d-0242ac130003',
        '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'Tiltaksgjennomforing1', 'GJENNOMFORES', current_date,
        current_date, current_timestamp, current_timestamp);

INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling,
                      registrert_dato)
VALUES
    (deltaker1, brukerId1,
     'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE, 'tomorrow'::DATE, 5, 100,
     'yesterday'::DATE),
    (deltaker2, brukerId2,
     'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE, 'tomorrow'::DATE, 5, 100,
     'yesterday'::DATE),
    (deltaker3, brukerId3,
     'b3420940-5479-48c8-b2fa-3751c7a33aa2', 'yesterday'::DATE, 'tomorrow'::DATE, 5, 100,
     'yesterday'::DATE);

INSERT INTO deltaker_status (id, deltaker_id, endret_dato, status, aktiv)
VALUES
       ('f07e3938-95d4-4951-a483-9ffbf490601d', deltaker1, 'yesterday'::DATE, 'VENTER_PA_OPPSTART', true),
       ('6d7d8b65-5c19-4430-a367-df9c751d46b4', deltaker2, 'yesterday'::DATE, 'DELTAR', true),
       ('36eeb6ea-4738-4829-9362-e58252d9d712', deltaker3, 'yesterday'::DATE, 'DELTAR', true);

END $$;