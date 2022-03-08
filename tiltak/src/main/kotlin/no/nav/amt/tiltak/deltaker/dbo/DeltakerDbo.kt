package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerDbo(
	val id: UUID,
	val brukerId: UUID,
	val brukerFodselsnummer: String,
	val brukerFornavn: String,
	val brukerMellomnavn: String? = null,
	val brukerEtternavn: String,
	val gjennomforingId: UUID? = null,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
	val createdAt: LocalDateTime? = null,
	// Setter modified til nå. Dersom oppretter objektet, så er det to grunner til det.
	// Enten hentes fra db og man leser verdi fra db, ellers oppretter man dbo-objektet for å oppdatere/inserte i db
	val modifiedAt: LocalDateTime? = LocalDateTime.now(),
	val registrertDato: LocalDateTime
) {

	constructor(deltaker: Deltaker) :
		this(
			id = deltaker.id,
			brukerId = requireNotNull(deltaker.bruker?.id),
			brukerFodselsnummer = requireNotNull(deltaker.bruker?.fodselsnummer),
			brukerFornavn = requireNotNull(deltaker.bruker?.fornavn),
			brukerEtternavn = requireNotNull(deltaker.bruker?.etternavn),
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato
		)

	fun toDeltaker(statusProvider: (deltakerId: UUID) -> List<DeltakerStatusDbo>): Deltaker {
		return Deltaker(
			id = id,
			bruker = Bruker(
				id = brukerId,
				fornavn = brukerFornavn,
				mellomnavn = brukerMellomnavn,
				etternavn = brukerEtternavn,
				fodselsnummer = brukerFodselsnummer,
			),
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato,
			statuser = statusProvider(id).toDeltakerStatuser()
		)
	}

}



