package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
	val navVeilederId: UUID?,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
	val createdAt: LocalDateTime? = null,
	val modifiedAt: LocalDateTime,
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
			navVeilederId = navVeilederId,
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato,
			status = status,
			innsokBegrunnelse = innsokBegrunnelse,
			endretDato = modifiedAt
		)
	}

}



