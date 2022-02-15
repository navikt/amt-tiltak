package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.test.database.data.commands.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object TestData1 {

	val ARRANGOR_1 = InsertArrangorCommand(
		id = UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003"),
		overordnet_enhet_organisasjonsnummer = "911111111",
		overordnet_enhet_navn = "Org Tiltaksarrangør 1",
		organisasjonsnummer = "111111111",
		navn = "Tiltaksarrangør 1"
	)

	val ARRANGOR_2 = InsertArrangorCommand(
		id = UUID.fromString("bd9ea032-03a3-4dbf-96ec-8a2a69b5c92a"),
		overordnet_enhet_organisasjonsnummer = "922222222",
		overordnet_enhet_navn = "Org Tiltaksarrangør 2",
		organisasjonsnummer = "222222222",
		navn = "Tiltaksarrangør 2"
	)


	val ARRANGOR_ANSATT_1 = InsertArrangorAnsattCommand(
		id = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f"),
		personlig_ident = "123456789",
		fornavn = "Ansatt 1 fornavn",
		etternavn = "Ansatt 1 etternavn"
	)

	val ARRANGOR_ANSATT_1_ROLLE_1 = InsertArrangorAnsattRolleCommand(
		id = UUID.fromString("e0a0eda3-9f72-4330-8e34-7fb5176fe123"),
		arrangor_id = ARRANGOR_1.id,
		ansatt_id = ARRANGOR_ANSATT_1.id,
		rolle = "KOORDINATOR"
	)

	val ARRANGOR_ANSATT_1_ROLLE_2 = InsertArrangorAnsattRolleCommand(
		id = UUID.fromString("9c7db3c9-bb61-460b-be2a-0fdbcae912ab"),
		arrangor_id = ARRANGOR_1.id,
		ansatt_id = ARRANGOR_ANSATT_1.id,
		rolle = "VEILEDER"
	)


	val ARRANGOR_ANSATT_2 = InsertArrangorAnsattCommand(
		id = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f"),
		personlig_ident = "123456789",
		fornavn = "Ansatt 1 fornavn",
		etternavn = "Ansatt 1 etternavn"
	)

	val ARRANGOR_ANSATT_2_ROLLE_1 = InsertArrangorAnsattRolleCommand(
		id = UUID.fromString("9892eb04-19b8-4244-8953-90d75d20a50c"),
		arrangor_id = ARRANGOR_1.id,
		ansatt_id = ARRANGOR_ANSATT_2.id,
		rolle = "VEILEDER"
	)


	val TILTAK_1 = InsertTiltakCommand(
		id = UUID.fromString("b18fd670-3bc1-11ec-8d3d-0242ac130003"),
		navn = "Tiltak1",
		type = "AMO"
	)

	val GJENNOMFORING_1 = InsertGjennomforingCommand(
		id = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2"),
		tiltak_id = TILTAK_1.id,
		arrangor_id = ARRANGOR_1.id,
		navn = "Tiltaksgjennomforing1",
		status = "GJENNOMFORES",
		start_dato = LocalDate.of(2022, 2, 1),
		slutt_dato = LocalDate.of(2050, 12, 30),
		registrert_dato = LocalDate.of(2022, 1, 1),
		fremmote_dato = LocalDate.of(2022, 2, 1)
	)

	val NAV_ANSATT_1 = InsertNavAnsattCommand(
		id = UUID.fromString("91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe"),
		nav_ident = "Z4321",
		navn = "Vashnir Veiledersen",
		telefonnummer = "88776655",
		epost = "vashnir.veiledersen@nav.no"
	)

	val NAV_ANSATT_2 = InsertNavAnsattCommand(
		id = UUID.fromString("5e8790a9-7339-4ea2-ae75-54aac33f6c4d"),
		nav_ident = "Z1234",
		navn = "Ola Nordmann",
		telefonnummer = "99887654",
		epost = "ola.nordmann@nav.no"
	)

	val NAV_KONTOR_1 = InsertNavKontorCommand(
		id = UUID.fromString("09405517-99c0-49e5-9eb3-31c61b9579cf"),
		enhet_id = "1234",
		navn = "NAV Testheim"
	)


	// Bruker 1

	val BRUKER_1 = InsertBrukerCommand(
		id = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8"),
		fodselsnummer = "12345678910",
		fornavn = "Bruker 1 fornavn",
		etternavn = "Bruker 1 etternavn",
		telefonnummer = "73404782",
		epost = "bruker1@example.com",
		ansvarlig_veileder_id = NAV_ANSATT_1.id,
		nav_kontor_id = NAV_KONTOR_1.id
	)

	val DELTAKER_1 = InsertDeltakerCommand(
		id = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214"),
		bruker_id = BRUKER_1.id,
		gjennomforing_id = GJENNOMFORING_1.id,
		start_dato = LocalDate.of(2022, 2, 13),
		slutt_dato = LocalDate.of(2030, 2, 14),
		dager_per_uke = 5,
		prosent_stilling = 100,
		registrert_dato = LocalDate.of(2022, 2, 13)
	)

	val DELTAKER_1_STATUS_1 = InsertDeltakerStatusCommand(
		id = UUID.fromString("d6770809-29e3-47e0-8cc2-4fa667d1a756"),
		deltaker_id = DELTAKER_1.id,
		endret_dato = ZonedDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	// Bruker 2


	val BRUKER_2 = InsertBrukerCommand(
		id = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8"),
		fodselsnummer = "7908432423",
		fornavn = "Bruker 2 fornavn",
		etternavn = "Bruker 2 etternavn",
		telefonnummer = "65443532",
		epost = "bruker2@example.com",
		ansvarlig_veileder_id = null,
		nav_kontor_id = NAV_KONTOR_1.id
	)

	val DELTAKER_2 = InsertDeltakerCommand(
		id = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214"),
		bruker_id = BRUKER_2.id,
		gjennomforing_id = GJENNOMFORING_1.id,
		start_dato = LocalDate.of(2022, 2, 10),
		slutt_dato = LocalDate.of(2022, 2, 12),
		dager_per_uke = 5,
		prosent_stilling = 100,
		registrert_dato = LocalDate.of(2022, 2, 10)
	)

	val DELTAKER_2_STATUS_1 = InsertDeltakerStatusCommand(
		id = UUID.fromString("d6770809-29e3-47e0-8cc2-4fa667d1a756"),
		deltaker_id = DELTAKER_2.id,
		endret_dato = ZonedDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	// Bruker 3

	val BRUKER_3 = InsertBrukerCommand(
		id = UUID.fromString("c74abcdd-53a7-4e22-86f3-97de9094029f"),
		fodselsnummer = "3792473283",
		fornavn = "Bruker 3 fornavn",
		etternavn = "Bruker 3 etternavn",
		telefonnummer = "39057809",
		epost = "bruker3@example.com",
		ansvarlig_veileder_id = null,
		nav_kontor_id = NAV_KONTOR_1.id
	)

}

object TestData {

	val ARRANGOR_1_ID: UUID = UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003")

	val ARRANGOR_2_ID: UUID = UUID.fromString("bd9ea032-03a3-4dbf-96ec-8a2a69b5c92a")


	val ARRANGOR_ANSATT_1_FNR = "123456789"

	val ARRANGOR_ANSATT_1_ID: UUID = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f")


	val ARRANGOR_ANSATT_2_FNR = "732098472"


	val TILTAK_1_ID: UUID = UUID.fromString("b18fd670-3bc1-11ec-8d3d-0242ac130003")


	val NAV_KONTOR_1_ID: UUID = UUID.fromString("09405517-99c0-49e5-9eb3-31c61b9579cf")

	val NAV_KONTOR_1_ENHET_ID = "1234"


	val VEILEDER_1_ID: UUID = UUID.fromString("91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe")

	val VEILEDER_1_NAV_IDENT = "Z4321"


	val VEILEDER_2_ID: UUID = UUID.fromString("5e8790a9-7339-4ea2-ae75-54aac33f6c4d")


	val BRUKER_1_FNR = "12345678910"

	val BRUKER_1_ID: UUID = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8")


	val BRUKER_3_ID: UUID = UUID.fromString("c74abcdd-53a7-4e22-86f3-97de9094029f")

	val BRUKER_3_FNR = "3792473283"


	val DELTAKER_1_ID: UUID = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214")


	val DELTAKER_2_ID: UUID = UUID.fromString("daaac46f-c7af-4028-96c3-8fa81ec2b93c")


	val GJENNOMFORING_1_ID: UUID = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")

}
