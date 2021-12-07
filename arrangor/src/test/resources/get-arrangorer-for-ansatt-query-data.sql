
INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, etternavn, telefonnummer, epost)
VALUES('6321c7dc-6cfb-47b0-b566-32979be5041f', '123456789', 'Test', 'Testersen', '1234', 'test@test.no');


INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn', '2', 'VirkNavn');


INSERT INTO arrangor_ansatt_rolle(id, ansatt_id, arrangor_id, rolle)
VALUES ('574aae6f-d0e1-43e1-a4e6-9434fb8f335f', '6321c7dc-6cfb-47b0-b566-32979be5041f', '8a37bce6-3bc1-11ec-8d3d-0242ac130003', 'KOORDINATOR');