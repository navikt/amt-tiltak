package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerDbo(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val personIdent: String,
	val erSkjermet: Boolean,
	val navEnhetId: UUID?,
	val navKontor: String?,
	val navVeilederId: UUID?,
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

	fun toDeltaker(status: DeltakerStatus): Deltaker {
		return Deltaker(
			id = id,
			gjennomforingId = gjennomforingId,
			fornavn = fornavn,
			mellomnavn = mellomnavn,
			etternavn = etternavn,
			telefonnummer = telefonnummer,
			epost = epost,
			personIdent = personIdent,
			erSkjermet = erSkjermet,
			navEnhetId = navEnhetId,
			navKontor = navKontor,
			navVeilederId = navVeilederId,
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



