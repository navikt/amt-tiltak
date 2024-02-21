package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.DeltakelsesInnhold
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.nav_enhet.NavEnhetDbo
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
	val navEnhet: NavEnhetDbo?,
	val navVeilederId: UUID?,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Float?,
	val prosentStilling: Float?,
	val createdAt: LocalDateTime? = null,
	val modifiedAt: LocalDateTime,
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?,
	val adressebeskyttelse: Adressebeskyttelse?,
	val innhold: DeltakelsesInnhold?,
	val kilde: Kilde?
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
			navEnhet = navEnhet?.toNavEnhet(),
			navVeilederId = navVeilederId,
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato,
			status = status,
			innsokBegrunnelse = innsokBegrunnelse,
			endretDato = modifiedAt,
			adressebeskyttelse = adressebeskyttelse,
			innhold = innhold,
			kilde = kilde
		)
	}

}



