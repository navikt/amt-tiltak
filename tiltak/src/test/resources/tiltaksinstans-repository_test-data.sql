INSERT INTO tiltaksleverandor(id, external_id, organisasjonsnummer, organisasjonsnavn, virksomhetsnummer,
                              virksomhetsnavn)
VALUES (1, '0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e', '12345678', 'Orgnavn1', '87654321', 'Virksomhetsnavn1');

INSERT INTO tiltak(external_id, arena_id, tiltaksleverandor_id, navn, type)
VALUES ('9665b0b6-ea7d-44b0-b9c2-8867c2a6c106', '1', 1, 'Test tiltak 1', 'AMO');
