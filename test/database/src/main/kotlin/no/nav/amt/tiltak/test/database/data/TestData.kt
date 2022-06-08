package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.test.database.data.commands.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object TestData {

	fun createDeltakerCommand(bruker: InsertBrukerCommand, gjennomforing: InsertGjennomforingCommand) =
		InsertDeltakerCommand(
			id = UUID.randomUUID(),
			bruker_id = bruker.id,
			gjennomforing_id = gjennomforing.id,
			start_dato = LocalDate.of(2022, 2, 13),
			slutt_dato = LocalDate.of(2030, 2, 14),
			dager_per_uke = 5,
			prosent_stilling = 100f,
			registrert_dato = LocalDateTime.now()
		)

	fun createBrukerCommand(navEnhet: InsertNavEnhetCommand) =
		InsertBrukerCommand(
			id = UUID.randomUUID(),
			fodselsnummer = (1000..9999).random().toString(),
			fornavn = "Fornavn",
			etternavn = "Etternavn",
			telefonnummer = (1000..9999).random().toString(),
			epost = "bruker@example.com",
			ansvarlig_veileder_id = null,
			nav_enhet_id = navEnhet.id
		)

	fun createStatusCommand(deltaker: InsertDeltakerCommand) = InsertDeltakerStatusCommand(
		id = UUID.randomUUID(),
		deltaker_id = deltaker.id,
		gyldigFra = LocalDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	val NAV_ENHET_1 = InsertNavEnhetCommand(
		id = UUID.fromString("09405517-99c0-49e5-9eb3-31c61b9579cf"),
		enhet_id = "1234",
		navn = "NAV Testheim"
	)

	val NAV_ENHET_2 = InsertNavEnhetCommand(
		id = UUID.fromString("a1247bfb-255f-411d-b3ae-6d6d29a4fa58"),
		enhet_id = "5678",
		navn = "NAV Test"
	)

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

	val ARRANGOR_3 = InsertArrangorCommand(
		id = UUID.fromString("96d12a44-7db2-49fb-bbac-64da5e7536c0"),
		overordnet_enhet_organisasjonsnummer = "933333333",
		overordnet_enhet_navn = "Org Tiltaksarrangør 3",
		organisasjonsnummer = "333333333",
		navn = "Tiltaksarrangør 3"
	)


	val ARRANGOR_ANSATT_1 = InsertArrangorAnsattCommand(
		id = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f"),
		personlig_ident = "123456789",
		fornavn = "Ansatt 1 fornavn",
		mellomnavn = "Ansatt 1 mellomnavn",
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
		arrangor_id = ARRANGOR_2.id,
		ansatt_id = ARRANGOR_ANSATT_1.id,
		rolle = "VEILEDER"
	)

	val ARRANGOR_ANSATT_2 = InsertArrangorAnsattCommand(
		id = UUID.fromString("a24e659c-2651-4fbb-baad-01cacb2412f0"),
		personlig_ident = "326749823",
		fornavn = "Ansatt 2 fornavn",
		mellomnavn = null,
		etternavn = "Ansatt 2 etternavn"
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
		nav_enhet_id = NAV_ENHET_1.id,
		registrert_dato = LocalDate.of(2022, 1, 1),
		fremmote_dato = LocalDate.of(2022, 2, 1),
		opprettet_aar = 2020,
		lopenr = 123
	)

	val GJENNOMFORING_2 = InsertGjennomforingCommand(
		id = UUID.fromString("513219ca-481b-4aae-9d51-435dba9929cd"),
		tiltak_id = TILTAK_1.id,
		arrangor_id = ARRANGOR_2.id,
		navn = "Tiltaksgjennomforing2",
		status = "AVSLUTTET",
		start_dato = LocalDate.of(2022, 2, 1),
		slutt_dato = LocalDate.of(2022, 2, 13),
		nav_enhet_id = NAV_ENHET_2.id,
		registrert_dato = LocalDate.of(2022, 1, 1),
		fremmote_dato = LocalDate.of(2022, 2, 1),
		opprettet_aar = 2020,
		lopenr = 124
	)

	val GJENNOMFORING_TILGANG_1 = InsertArrangorAnsattGjennomforingTilgang(
		id = UUID.randomUUID(),
		ansatt_id = ARRANGOR_ANSATT_1.id,
		gjennomforing_id = GJENNOMFORING_1.id
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


	// Bruker 1

	val BRUKER_1 = InsertBrukerCommand(
		id = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8"),
		fodselsnummer = "12345678910",
		fornavn = "Bruker 1 fornavn",
		etternavn = "Bruker 1 etternavn",
		telefonnummer = "73404782",
		epost = "bruker1@example.com",
		ansvarlig_veileder_id = NAV_ANSATT_1.id,
		nav_enhet_id = NAV_ENHET_1.id
	)

	val DELTAKER_1 = InsertDeltakerCommand(
		id = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214"),
		bruker_id = BRUKER_1.id,
		gjennomforing_id = GJENNOMFORING_1.id,
		start_dato = LocalDate.of(2022, 2, 13),
		slutt_dato = LocalDate.of(2030, 2, 14),
		dager_per_uke = 5,
		prosent_stilling = 100f,
		registrert_dato = LocalDateTime.of(2022, 2, 13, 12, 12)
	)


	val DELTAKER_1_STATUS_1 = InsertDeltakerStatusCommand(
		id = UUID.fromString("d6770809-29e3-47e0-8cc2-4fa667d1a756"),
		deltaker_id = DELTAKER_1.id,
		gyldigFra = LocalDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	// Bruker 2


	val BRUKER_2 = InsertBrukerCommand(
		id = UUID.fromString("170a1323-03d8-4580-a1f8-bc14a0422da6"),
		fodselsnummer = "7908432423",
		fornavn = "Bruker 2 fornavn",
		etternavn = "Bruker 2 etternavn",
		telefonnummer = "65443532",
		epost = "bruker2@example.com",
		ansvarlig_veileder_id = null,
		nav_enhet_id = NAV_ENHET_1.id
	)

	val DELTAKER_2 = InsertDeltakerCommand(
		id = UUID.fromString("8a0b7158-4d5e-4563-88be-b9bce5662879"),
		bruker_id = BRUKER_2.id,
		gjennomforing_id = GJENNOMFORING_1.id,
		start_dato = LocalDate.of(2022, 2, 10),
		slutt_dato = LocalDate.of(2022, 2, 12),
		dager_per_uke = 5,
		prosent_stilling = 100f,
		registrert_dato = LocalDateTime.of(2022, 2, 10, 12, 12)
	)

	val DELTAKER_2_STATUS_1 = InsertDeltakerStatusCommand(
		id = UUID.fromString("227b67ea-92ca-4b94-9588-89209b01c0e5"),
		deltaker_id = DELTAKER_2.id,
		gyldigFra = LocalDateTime.now(),
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
		nav_enhet_id = NAV_ENHET_1.id
	)

	// Bruker 4

	val BRUKER_4 = InsertBrukerCommand(
		id = UUID.fromString("6b5d7600-a9a6-4918-8e8a-068d1c1f5a65"),
		fodselsnummer = "10028029182",
		fornavn = "Bruker 4 fornavn",
		etternavn = "Bruker 4 etternavn",
		telefonnummer = "11223344",
		epost = "bruker4@example.com",
		ansvarlig_veileder_id = null,
		nav_enhet_id = NAV_ENHET_1.id
	)


	val DELTAKER_4 = InsertDeltakerCommand(
		id = UUID.fromString("65ea8f52-6140-4fbd-810e-4fe1908fd6e7"),
		bruker_id = BRUKER_4.id,
		gjennomforing_id = GJENNOMFORING_1.id,
		start_dato = LocalDate.of(2022, 2, 10),
		slutt_dato = LocalDate.of(2022, 2, 12),
		dager_per_uke = 5,
		prosent_stilling = 100f,
		registrert_dato = LocalDateTime.of(2022, 2, 10, 12, 12)
	)

	val DELTAKER_4_STATUS_1 = InsertDeltakerStatusCommand(
		id = UUID.fromString("b9cb7403-fda6-42d9-a011-8712f4a37801"),
		deltaker_id = DELTAKER_4.id,
		gyldigFra = LocalDateTime.now(),
		status = "VENTER_PA_OPPSTART",
		aktiv = true
	)

}
