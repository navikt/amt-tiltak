package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.data_publisher.model.AnsattRolle
import no.nav.amt.tiltak.data_publisher.model.VeilederType
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

class DatabaseTestDataHandler(template: NamedParameterJdbcTemplate) {

	val testDataRepository = TestDataRepository(template)

	fun createArrangor(): ArrangorInput = arrangorInput()
		.also { testDataRepository.insertArrangor(it) }


	fun updateArrangor(input: ArrangorInput) = testDataRepository.updateArrangor(input)

	fun createDeltakerliste(
		arrangorId: UUID = createArrangor().id,
		tiltakId: UUID = createTiltak().id
	): GjennomforingInput =
		gjennomforingInput(tiltakId = tiltakId, arrangorId = arrangorId)
			.also { testDataRepository.insertGjennomforing(it) }


	fun createTiltak(): TiltakInput = tiltakInput()
		.also { testDataRepository.insertTiltak(it) }

	fun updateTiltak(input: TiltakInput) = testDataRepository.updateTiltak(input)

	fun createDeltaker(
		brukerId: UUID = createBruker().id,
		gjennomforingId: UUID = createDeltakerliste().id
	): DeltakerInput = deltakerInput(brukerId, gjennomforingId)
		.also { testDataRepository.insertDeltaker(it) }

	fun createBruker(
		ansvarligVeilederId: UUID? = createNavAnsatt().id,
		navEnhetId: UUID = createNavEnhet().id
	): BrukerInput = brukerInput(ansvarligVeilederId, navEnhetId)
		.also { testDataRepository.insertBruker(it) }

	fun createNavEnhet(): NavEnhetInput = navEnhetInput()
		.also { testDataRepository.insertNavEnhet(it) }


	fun createNavAnsatt(): NavAnsattInput = navAnsattInput()
		.also { testDataRepository.insertNavAnsatt(it) }


	private fun arrangorInput(
		id: UUID = UUID.randomUUID(),
		navn: String = UUID.randomUUID().toString(),
		organisasjonsnummer: String = UUID.randomUUID().toString(),
		overordnetEnhetNavn: String = UUID.randomUUID().toString(),
		overordnetEnhetOrganisasjonsnummer: String = UUID.randomUUID().toString()
	): ArrangorInput = ArrangorInput(
		id, overordnetEnhetOrganisasjonsnummer, overordnetEnhetNavn, organisasjonsnummer, navn
	)

	private fun arrangorAnsattInput(
		id: UUID = UUID.randomUUID(),
		personligIdent: String = UUID.randomUUID().toString(),
		fornavn: String = UUID.randomUUID().toString(),
		mellomnavn: String? = null,
		etternavn: String = UUID.randomUUID().toString()
	): ArrangorAnsattInput = ArrangorAnsattInput(id, personligIdent, fornavn, mellomnavn, etternavn)

	private fun ansattRolleInput(
		id: UUID = UUID.randomUUID(),
		arrangorId: UUID,
		ansattId: UUID,
		rolle: AnsattRolle
	): ArrangorAnsattRolleInput = ArrangorAnsattRolleInput(id, arrangorId, ansattId, rolle.name)

	private fun tiltakInput(
		id: UUID = UUID.randomUUID(),
		navn: String = UUID.randomUUID().toString(),
		type: String = UUID.randomUUID().toString()
	): TiltakInput = TiltakInput(id, navn, type)

	private fun gjennomforingInput(
		id: UUID = UUID.randomUUID(),
		tiltakId: UUID,
		arrangorId: UUID,
		status: String = "GJENNOMFORES",
		navn: String = UUID.randomUUID().toString(),
		startDato: LocalDate = LocalDate.now().minusDays(1),
		sluttDato: LocalDate = LocalDate.now().plusDays(1),
		navEnhetId: UUID? = null,
		opprettetAar: Int = LocalDate.now().year,
		lopenr: Int = Random().nextInt()
	): GjennomforingInput = GjennomforingInput(
		id, tiltakId, arrangorId, navn, status, startDato, sluttDato, navEnhetId, opprettetAar, lopenr
	)

	private fun navEnhetInput(): NavEnhetInput = NavEnhetInput(
		id = UUID.randomUUID(),
		enhetId = UUID.randomUUID().toString(),
		navn = UUID.randomUUID().toString()
	)

	private fun brukerInput(
		ansvarligVeilederId: UUID?,
		navEnhetId: UUID
	): BrukerInput = BrukerInput(
		id = UUID.randomUUID(),
		personIdent = UUID.randomUUID().toString(),
		fornavn = UUID.randomUUID().toString(),
		mellomnavn = null,
		etternavn = UUID.randomUUID().toString(),
		telefonnummer = UUID.randomUUID().toString(),
		epost = UUID.randomUUID().toString(),
		ansvarligVeilederId = ansvarligVeilederId,
		navEnhetId = navEnhetId,
		erSkjermet = false
	)

	private fun deltakerInput(
		brukerId: UUID,
		gjennomforingId: UUID
	): DeltakerInput = DeltakerInput(
		id = UUID.randomUUID(),
		brukerId = brukerId,
		gjennomforingId = gjennomforingId,
		startDato = LocalDate.now(),
		sluttDato = LocalDate.now().plusDays(1),
		dagerPerUke = 5,
		prosentStilling = 100.0F,
		registrertDato = LocalDateTime.now(),
		innsokBegrunnelse = UUID.randomUUID().toString()
	)

	private fun navAnsattInput(): NavAnsattInput = NavAnsattInput(
		id = UUID.randomUUID(),
		navIdent = UUID.randomUUID().toString(),
		navn = UUID.randomUUID().toString(),
		telefonnummer = UUID.randomUUID().toString(),
		epost = UUID.randomUUID().toString()
	)

	private fun arrangorVeilederInput(
		ansattId: UUID,
		deltakerId: UUID,
		type: VeilederType = VeilederType.VEILEDER
	) = ArrangorVeilederDboInput(
		id = UUID.randomUUID(),
		ansattId = ansattId,
		deltakerId = deltakerId,
		erMedveileder = type == VeilederType.MEDVEILEDER,
		gyldigFra = ZonedDateTime.now().minusDays(1),
		gyldigTil = ZonedDateTime.now().plusDays(1)
	)

}
