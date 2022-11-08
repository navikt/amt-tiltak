package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.test.database.data.inputs.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

object TestData {

	fun createGjennomforingInput(tiltak: TiltakInput, arrangor: ArrangorInput, enhet: NavEnhetInput) =
		GjennomforingInput(
			id = UUID.randomUUID(),
			tiltakId = tiltak.id,
			arrangorId = arrangor.id,
			navn = "Tiltaksgjennomforing1",
			status = "GJENNOMFORES",
			startDato = LocalDate.now().minusWeeks(3),
			sluttDato = LocalDate.now().plusYears(3),
			navEnhetId = enhet.id,
			registrertDato = LocalDate.now().minusWeeks(4),
			fremmoteDato = LocalDate.of(2022, 2, 1),
			opprettetAar = 2020,
			lopenr = 123
		)

	fun createDeltakerInput(bruker: BrukerInput, gjennomforing: GjennomforingInput) =
		DeltakerInput(
			id = UUID.randomUUID(),
			brukerId = bruker.id,
			gjennomforingId = gjennomforing.id,
			startDato = LocalDate.now().plusDays(5),
			sluttDato = LocalDate.now().plusDays(30),
			dagerPerUke = 5,
			prosentStilling = 100f,
			registrertDato = LocalDateTime.now(),
			innsokBegrunnelse = null
		)

	fun createBrukerInput(navEnhet: NavEnhetInput) =
		BrukerInput(
			id = UUID.randomUUID(),
			fodselsnummer = (1000..9999).random().toString(),
			fornavn = "Fornavn",
			etternavn = "Etternavn",
			telefonnummer = (1000..9999).random().toString(),
			epost = "bruker@example.com",
			ansvarligVeilederId = null,
			navEnhetId = navEnhet.id
		)

	fun createStatusInput(deltaker: DeltakerInput) = DeltakerStatusInput(
		id = UUID.randomUUID(),
		deltakerId = deltaker.id,
		gyldigFra = LocalDateTime.now(),
		status = "DELTAR",
		aktiv = true,
		createdAt = deltaker.createdAt
	)

	val NAV_ENHET_1 = NavEnhetInput(
		id = UUID.fromString("09405517-99c0-49e5-9eb3-31c61b9579cf"),
		enhetId = "1234",
		navn = "NAV Testheim"
	)

	val NAV_ENHET_2 = NavEnhetInput(
		id = UUID.fromString("a1247bfb-255f-411d-b3ae-6d6d29a4fa58"),
		enhetId = "5678",
		navn = "NAV Test"
	)

	val ARRANGOR_1 = ArrangorInput(
		id = UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003"),
		overordnetEnhetOrganisasjonsnummer = "911111111",
		overordnetEnhetNavn = "Org Tiltaksarrangør 1",
		organisasjonsnummer = "111111111",
		navn = "Tiltaksarrangør 1"
	)

	val ARRANGOR_2 = ArrangorInput(
		id = UUID.fromString("bd9ea032-03a3-4dbf-96ec-8a2a69b5c92a"),
		overordnetEnhetOrganisasjonsnummer = "922222222",
		overordnetEnhetNavn = "Org Tiltaksarrangør 2",
		organisasjonsnummer = "222222222",
		navn = "Tiltaksarrangør 2"
	)

	val ARRANGOR_3 = ArrangorInput(
		id = UUID.fromString("96d12a44-7db2-49fb-bbac-64da5e7536c0"),
		overordnetEnhetOrganisasjonsnummer = "933333333",
		overordnetEnhetNavn = "Org Tiltaksarrangør 3",
		organisasjonsnummer = "333333333",
		navn = "Tiltaksarrangør 3"
	)


	val ARRANGOR_ANSATT_1 = ArrangorAnsattInput(
		id = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f"),
		personligIdent = "123456789",
		fornavn = "Ansatt 1 fornavn",
		mellomnavn = "Ansatt 1 mellomnavn",
		etternavn = "Ansatt 1 etternavn"
	)

	val ARRANGOR_ANSATT_1_ROLLE_1 = ArrangorAnsattRolleInput(
		id = UUID.fromString("e0a0eda3-9f72-4330-8e34-7fb5176fe123"),
		arrangorId = ARRANGOR_1.id,
		ansattId = ARRANGOR_ANSATT_1.id,
		rolle = "KOORDINATOR"
	)

	val ARRANGOR_ANSATT_1_ROLLE_2 = ArrangorAnsattRolleInput(
		id = UUID.fromString("9c7db3c9-bb61-460b-be2a-0fdbcae912ab"),
		arrangorId = ARRANGOR_2.id,
		ansattId = ARRANGOR_ANSATT_1.id,
		rolle = "VEILEDER"
	)

	val ARRANGOR_ANSATT_2 = ArrangorAnsattInput(
		id = UUID.fromString("a24e659c-2651-4fbb-baad-01cacb2412f0"),
		personligIdent = "326749823",
		fornavn = "Ansatt 2 fornavn",
		mellomnavn = null,
		etternavn = "Ansatt 2 etternavn"
	)

	val ARRANGOR_ANSATT_2_ROLLE_1 = ArrangorAnsattRolleInput(
		id = UUID.fromString("9892eb04-19b8-4244-8953-90d75d20a50c"),
		arrangorId = ARRANGOR_1.id,
		ansattId = ARRANGOR_ANSATT_2.id,
		rolle = "VEILEDER"
	)


	val TILTAK_1 = TiltakInput(
		id = UUID.fromString("b18fd670-3bc1-11ec-8d3d-0242ac130003"),
		navn = "Tiltak1",
		type = "AMO"
	)

	val GJENNOMFORING_1 = GjennomforingInput(
		id = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2"),
		tiltakId = TILTAK_1.id,
		arrangorId = ARRANGOR_1.id,
		navn = "Tiltaksgjennomforing1",
		status = "GJENNOMFORES",
		startDato = LocalDate.of(2022, 2, 1),
		sluttDato = LocalDate.of(2050, 12, 30),
		navEnhetId = NAV_ENHET_1.id,
		registrertDato = LocalDate.of(2022, 1, 1),
		fremmoteDato = LocalDate.of(2022, 2, 1),
		opprettetAar = 2020,
		lopenr = 123
	)

	val GJENNOMFORING_2 = GjennomforingInput(
		id = UUID.fromString("513219ca-481b-4aae-9d51-435dba9929cd"),
		tiltakId = TILTAK_1.id,
		arrangorId = ARRANGOR_2.id,
		navn = "Tiltaksgjennomforing2",
		status = "AVSLUTTET",
		startDato = LocalDate.of(2022, 2, 1),
		sluttDato = LocalDate.of(2022, 2, 13),
		navEnhetId = NAV_ENHET_2.id,
		registrertDato = LocalDate.of(2022, 1, 1),
		fremmoteDato = LocalDate.of(2022, 2, 1),
		opprettetAar = 2020,
		lopenr = 124
	)

	val GJENNOMFORING_3 = GjennomforingInput(
		id = UUID.fromString("44dacce9-3a2f-49b7-aaf1-e2c3dc4984a4"),
		tiltakId = TILTAK_1.id,
		arrangorId = ARRANGOR_2.id,
		navn = "Tiltaksgjennomforing2",
		status = "GJENNOMFORES",
		startDato = LocalDate.of(2022, 3, 5),
		sluttDato = LocalDate.of(2022, 7, 12),
		navEnhetId = NAV_ENHET_2.id,
		registrertDato = LocalDate.of(2022, 1, 1),
		fremmoteDato = LocalDate.of(2022, 2, 1),
		opprettetAar = 2022,
		lopenr = 439823
	)

	val GJENNOMFORING_TILGANG_1 = ArrangorAnsattGjennomforingTilgangInput(
		id = UUID.randomUUID(),
		ansattId = ARRANGOR_ANSATT_1.id,
		gjennomforingId = GJENNOMFORING_1.id,
		gyldigFra = ZonedDateTime.now().minusHours(1),
		gyldigTil = ZonedDateTime.now().plusYears(1)
	)

	val NAV_ANSATT_1 = NavAnsattInput(
		id = UUID.fromString("91e1c2e6-83cf-46f9-b8e0-4a9192c7dcbe"),
		navIdent = "Z4321",
		navn = "Vashnir Veiledersen",
		telefonnummer = "88776655",
		epost = "vashnir.veiledersen@nav.no"
	)

	val NAV_ANSATT_2 = NavAnsattInput(
		id = UUID.fromString("5e8790a9-7339-4ea2-ae75-54aac33f6c4d"),
		navIdent = "Z1234",
		navn = "Ola Nordmann",
		telefonnummer = "99887654",
		epost = "ola.nordmann@nav.no"
	)


	// Bruker 1

	val BRUKER_1 = BrukerInput(
		id = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8"),
		fodselsnummer = "12345678910",
		fornavn = "Bruker 1 fornavn",
		etternavn = "Bruker 1 etternavn",
		telefonnummer = "73404782",
		epost = "bruker1@example.com",
		ansvarligVeilederId = NAV_ANSATT_1.id,
		navEnhetId = NAV_ENHET_1.id
	)

	val DELTAKER_1 = DeltakerInput(
		id = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214"),
		brukerId = BRUKER_1.id,
		gjennomforingId = GJENNOMFORING_1.id,
		startDato = LocalDate.of(2022, 2, 13),
		sluttDato = LocalDate.of(2030, 2, 14),
		dagerPerUke = 5,
		prosentStilling = 100f,
		registrertDato = LocalDateTime.of(2022, 2, 13, 12, 12),
		innsokBegrunnelse = "begrunnelse deltaker 1",
	)


	val DELTAKER_1_STATUS_1 = DeltakerStatusInput(
		id = UUID.fromString("d6770809-29e3-47e0-8cc2-4fa667d1a756"),
		deltakerId = DELTAKER_1.id,
		gyldigFra = LocalDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	// Bruker 2


	val BRUKER_2 = BrukerInput(
		id = UUID.fromString("170a1323-03d8-4580-a1f8-bc14a0422da6"),
		fodselsnummer = "7908432423",
		fornavn = "Bruker 2 fornavn",
		etternavn = "Bruker 2 etternavn",
		telefonnummer = "65443532",
		epost = "bruker2@example.com",
		ansvarligVeilederId = null,
		navEnhetId = NAV_ENHET_1.id
	)

	val DELTAKER_2 = DeltakerInput(
		id = UUID.fromString("8a0b7158-4d5e-4563-88be-b9bce5662879"),
		brukerId = BRUKER_2.id,
		gjennomforingId = GJENNOMFORING_1.id,
		startDato = LocalDate.of(2022, 2, 10),
		sluttDato = LocalDate.of(2022, 2, 12),
		dagerPerUke = 5,
		prosentStilling = 100f,
		registrertDato = LocalDateTime.of(2022, 2, 10, 12, 12),
		innsokBegrunnelse = "begrunnelse deltaker 2"
	)

	val DELTAKER_2_STATUS_1 = DeltakerStatusInput(
		id = UUID.fromString("227b67ea-92ca-4b94-9588-89209b01c0e5"),
		deltakerId = DELTAKER_2.id,
		gyldigFra = LocalDateTime.now(),
		status = "DELTAR",
		aktiv = true
	)

	// Bruker 3

	val BRUKER_3 = BrukerInput(
		id = UUID.fromString("c74abcdd-53a7-4e22-86f3-97de9094029f"),
		fodselsnummer = "3792473283",
		fornavn = "Bruker 3 fornavn",
		etternavn = "Bruker 3 etternavn",
		telefonnummer = "39057809",
		epost = "bruker3@example.com",
		ansvarligVeilederId = null,
		navEnhetId = NAV_ENHET_1.id
	)

	// Bruker 4

	val BRUKER_4 = BrukerInput(
		id = UUID.fromString("6b5d7600-a9a6-4918-8e8a-068d1c1f5a65"),
		fodselsnummer = "10028029182",
		fornavn = "Bruker 4 fornavn",
		etternavn = "Bruker 4 etternavn",
		telefonnummer = "11223344",
		epost = "bruker4@example.com",
		ansvarligVeilederId = null,
		navEnhetId = NAV_ENHET_1.id
	)


	val DELTAKER_4 = DeltakerInput(
		id = UUID.fromString("65ea8f52-6140-4fbd-810e-4fe1908fd6e7"),
		brukerId = BRUKER_4.id,
		gjennomforingId = GJENNOMFORING_1.id,
		startDato = LocalDate.of(2022, 2, 10),
		sluttDato = LocalDate.of(2022, 2, 12),
		dagerPerUke = 5,
		prosentStilling = 100f,
		registrertDato = LocalDateTime.of(2022, 2, 10, 12, 12),
		innsokBegrunnelse = "begrunnelse deltaker 4"
	)

	val DELTAKER_4_STATUS_1 = DeltakerStatusInput(
		id = UUID.fromString("b9cb7403-fda6-42d9-a011-8712f4a37801"),
		deltakerId = DELTAKER_4.id,
		gyldigFra = LocalDateTime.now(),
		status = "VENTER_PA_OPPSTART",
		aktiv = true
	)

	val ENDRINGSMELDING_1_DELTAKER_1 = EndringsmeldingInput(
		id = UUID.fromString("9830e130-b18a-46b8-8e3e-6c06734d797e"),
		deltakerId = DELTAKER_1.id,
		opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
		status = Endringsmelding.Status.AKTIV,
		type = "LEGG_TIL_OPPSTARTSDATO",
		innhold = """{ "oppstartsdato": "${LocalDate.now()}" }""",
	)

	val ENDRINGSMELDING_2_DELTAKER_1 = EndringsmeldingInput(
		id = UUID.fromString("07099997-e02e-45e3-be6f-3c1eaf694557"),
		deltakerId = DELTAKER_1.id,
		opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
		status = Endringsmelding.Status.AKTIV,
		type = "AVSLUTT_DELTAKELSE",
		innhold = """{ "sluttdato": "${LocalDate.now()}", "aarsak": "ANNET" }""",
	)


	val ENDRINGSMELDING_1_DELTAKER_2 = EndringsmeldingInput(
		id = UUID.fromString("3fc16362-ba8b-4c0f-af93-b2ed56f12cd5"),
		deltakerId = DELTAKER_2.id,
		opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_2.id,
		status = Endringsmelding.Status.AKTIV,
		type = "LEGG_TIL_OPPSTARTSDATO",
		innhold = """{ "oppstartsdato": "${LocalDate.now()}" }""",
	)

	val NAV_ANSATT_1_GJENNOMFORING_1_TILGANG = TiltaksansvarligGjennomforingTilgangInput(
		id = UUID.randomUUID(),
		navAnsattId = NAV_ANSATT_1.id,
		gjennomforingId = GJENNOMFORING_1.id,
		gyldigTil = ZonedDateTime.now().plusDays(1),
		createdAt = ZonedDateTime.now()
	)

	val NAV_ANSATT_1_GJENNOMFORING_2_TILGANG = TiltaksansvarligGjennomforingTilgangInput(
		id = UUID.randomUUID(),
		navAnsattId = NAV_ANSATT_1.id,
		gjennomforingId = GJENNOMFORING_2.id,
		gyldigTil = ZonedDateTime.now().plusDays(1),
		createdAt = ZonedDateTime.now()
	)

}
