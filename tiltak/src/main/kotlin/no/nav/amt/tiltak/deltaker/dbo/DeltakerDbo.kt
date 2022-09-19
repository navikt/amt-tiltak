package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
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
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
	val createdAt: LocalDateTime? = null,
	// Setter modified til nå. Dersom oppretter objektet, så er det to grunner til det.
	// Enten hentes fra db og man leser verdi fra db, ellers oppretter man dbo-objektet for å oppdatere/inserte i db
	val modifiedAt: LocalDateTime? = LocalDateTime.now(),
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?
) {

	fun toDeltaker(status: DeltakerStatus, bruker: Bruker): Deltaker {
		return Deltaker(
			id = id,
			gjennomforingId = gjennomforingId,
			bruker = bruker,
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato,
			status = status,
			innsokBegrunnelse = innsokBegrunnelse
		)
	}

}



