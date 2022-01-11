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
	val brukerEtternavn: String,
	val gjennomforingId: UUID? = null,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
	val status: Deltaker.Status,
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
			// gjennomforingId = Denne lagrer vi vel aldri på nytt?
			startDato = deltaker.startDato,
			sluttDato = deltaker.startDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			status = deltaker.status,
			registrertDato = deltaker.registrertDato
		)

	fun toDeltaker(): Deltaker {
		return Deltaker(
			id = id,
			bruker = Bruker(
				id = brukerId,
				fornavn = brukerFornavn,
				etternavn = brukerEtternavn,
				fodselsnummer = brukerFodselsnummer,
			),
			startDato = startDato,
			sluttDato = sluttDato,
			status = status,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato
		)
	}

}



