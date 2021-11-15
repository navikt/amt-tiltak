INSERT INTO nav_ansatt (id, personlig_ident, fornavn, etternavn, telefonnummer, epost)
VALUES (1, '1234', 'Vashnir', 'Veiledersen', '84756', 'vashnir.veiledersen@nav.no');

INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id)
VALUES (1, '1', 'Bruker Fornavn', 'Bruker Etternavn', '384', 'm@2.c', 1);

INSERT INTO tiltaksleverandor(id, external_id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer,
                              navn)
VALUES (1, '8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');

INSERT INTO tiltak(id, external_id, arena_id, navn, type)
VALUES (1, 'b18fd670-3bc1-11ec-8d3d-0242ac130003', '1', 'Tiltak1', 'AMO');

INSERT INTO tiltaksinstans (id, external_id, arena_id, tiltak_id, tiltaksleverandor_id, navn, status, oppstart_dato,
                            slutt_dato, registrert_dato, fremmote_dato)
VALUES (1, 'b3420940-5479-48c8-b2fa-3751c7a33aa2', 1, 1, 1, 'Tiltaksinstans1', 'GJENNOMFORES', current_date,
        current_date, current_timestamp, current_timestamp);
