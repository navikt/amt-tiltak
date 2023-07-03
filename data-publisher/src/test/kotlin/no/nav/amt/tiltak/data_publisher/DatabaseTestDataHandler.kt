package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorInput
import no.nav.amt.tiltak.test.database.data.inputs.BrukerInput
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerInput
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerStatusInput
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.test.database.data.inputs.GjennomforingInput
import no.nav.amt.tiltak.test.database.data.inputs.NavAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.NavEnhetInput
import no.nav.amt.tiltak.test.database.data.inputs.TiltakInput
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Random
import java.util.UUID

class DatabaseTestDataHandler(template: NamedParameterJdbcTemplate) {

	val testDataRepository = TestDataRepository(template)

	fun createArrangor(): ArrangorInput = arrangorInput()
		.also { testDataRepository.insertArrangor(it) }

	fun createDeltakerliste(
		arrangorId: UUID = createArrangor().id,
		tiltakId: UUID = createTiltak().id
	): GjennomforingInput =
		gjennomforingInput(tiltakId = tiltakId, arrangorId = arrangorId)
			.also { testDataRepository.insertGjennomforing(it) }


	fun createTiltak(): TiltakInput = tiltakInput()
		.also { testDataRepository.insertTiltak(it) }

	fun createDeltaker(
		brukerId: UUID = createBruker().id,
		gjennomforingId: UUID = createDeltakerliste().id
	): DeltakerInput = deltakerInput(brukerId, gjennomforingId)
		.also { testDataRepository.insertDeltaker(it) }
		.also { testDataRepository.insertDeltakerStatus(createDeltakerStatus(it.id)) }

	fun createEndringsmelding(
		deltakerId: UUID = createDeltaker().id,
		opprettetAv: UUID = createArrangorAnsatt().id
	): EndringsmeldingInput = endringsmeldingInput(deltakerId, opprettetAv)
		.also { testDataRepository.insertEndringsmelding(it) }

	fun endringsmeldingInput(
		deltakerId: UUID,
		opprettetAv: UUID
	) = EndringsmeldingInput(
		id = UUID.randomUUID(),
		deltakerId = deltakerId,
		opprettetAvArrangorAnsattId = opprettetAv,
		type = Endringsmelding.Type.ENDRE_SLUTTDATO.name,
		innhold = JsonUtils.toJsonString(Endringsmelding.Innhold.EndreSluttdatoInnhold(LocalDate.now())),
		status = Endringsmelding.Status.AKTIV
	)

	fun createArrangorAnsatt() = arrangorAnsattInput()
		.also { testDataRepository.insertArrangorAnsatt(it) }

	fun arrangorAnsattInput() = ArrangorAnsattInput(
		id = UUID.randomUUID(),
		personligIdent = UUID.randomUUID().toString(),
		fornavn = UUID.randomUUID().toString(),
		etternavn = UUID.randomUUID().toString()
	)

	fun createDeltakerStatus(
		deltakerId: UUID
	): DeltakerStatusInput = DeltakerStatusInput(
		id = UUID.randomUUID(),
		deltakerId = deltakerId,
		gyldigFra = LocalDateTime.now(),
		status = DeltakerStatus.Type.DELTAR.name,
		aktiv = true,
		createdAt = ZonedDateTime.now()
	)

	fun createBruker(
		ansvarligVeilederId: UUID? = createNavAnsatt().id,
		navEnhet: NavEnhetInput = createNavEnhet()
	): BrukerInput = brukerInput(ansvarligVeilederId, navEnhet)
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
		id, tiltakId, arrangorId, navn, status, startDato, sluttDato, navEnhetId, opprettetAar, lopenr, false
	)

	private fun navEnhetInput(): NavEnhetInput = NavEnhetInput(
		id = UUID.randomUUID(),
		enhetId = UUID.randomUUID().toString(),
		navn = UUID.randomUUID().toString()
	)

	private fun brukerInput(
		ansvarligVeilederId: UUID?,
		navEnhet: NavEnhetInput?
	): BrukerInput = BrukerInput(
		id = UUID.randomUUID(),
		personIdent = UUID.randomUUID().toString(),
		fornavn = UUID.randomUUID().toString(),
		mellomnavn = null,
		etternavn = UUID.randomUUID().toString(),
		telefonnummer = UUID.randomUUID().toString(),
		epost = UUID.randomUUID().toString(),
		ansvarligVeilederId = ansvarligVeilederId,
		navEnhet = navEnhet,
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
}
