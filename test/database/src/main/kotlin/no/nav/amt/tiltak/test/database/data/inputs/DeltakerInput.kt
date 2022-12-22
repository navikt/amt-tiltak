package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class DeltakerInput(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val dagerPerUke: Int,
	val prosentStilling: Float,
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?,
	val createdAt: ZonedDateTime = ZonedDateTime.of(2022, 2, 13, 0, 0, 0, 0, ZoneId.systemDefault())
) {
	fun toDeltaker(brukerInput: BrukerInput, statusInput: DeltakerStatusInput) = Deltaker(
		id = id,
		gjennomforingId = gjennomforingId,
		fornavn = brukerInput.fornavn,
		mellomnavn = brukerInput.mellomnavn,
		etternavn = brukerInput.etternavn,
		telefonnummer = brukerInput.telefonnummer,
		erSkjermet = brukerInput.erSkjermet,
		epost = brukerInput.epost,
		personIdent = brukerInput.personIdent,
		navEnhetId = brukerInput.navEnhetId,
		navVeilederId = brukerInput.ansvarligVeilederId,
		startDato = startDato,
		sluttDato = sluttDato,
		status = DeltakerStatus(
			id = statusInput.id,
			type = DeltakerStatus.Type.valueOf(statusInput.status),
			aarsak = null,
			gyldigFra = statusInput.gyldigFra,
			opprettetDato = statusInput.createdAt.toLocalDateTime(),
			aktiv = statusInput.aktiv
		),
		registrertDato = registrertDato,
		dagerPerUke = dagerPerUke,
		prosentStilling = prosentStilling,
		innsokBegrunnelse = innsokBegrunnelse,
	)
}
