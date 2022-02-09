INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
    VALUES ('8a37bce6-3bc1-11ec-8d3d-0242ac130003', '1', 'OrgNavn1', '3', 'VirkNavn1');

INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
    VALUES ('71ca161f-f1d4-468c-a041-e72b8bbc0612', '2', 'OrgNavn2', '4', 'VirkNavn2');

INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, etternavn)
    VALUES('6321c7dc-6cfb-47b0-b566-32979be5041f', '123456789', 'Test', 'Testersen');

INSERT INTO arrangor_ansatt_rolle(id, arrangor_id, ansatt_id, rolle)
    VALUES('e0a0eda3-9f72-4330-8e34-7fb5176fe123', '8a37bce6-3bc1-11ec-8d3d-0242ac130003', '6321c7dc-6cfb-47b0-b566-32979be5041f', 'KOORDINATOR');

INSERT INTO arrangor_ansatt_rolle(id, arrangor_id, ansatt_id, rolle)
    VALUES('9c7db3c9-bb61-460b-be2a-0fdbcae912ab', '71ca161f-f1d4-468c-a041-e72b8bbc0612', '6321c7dc-6cfb-47b0-b566-32979be5041f', 'KOORDINATOR');



