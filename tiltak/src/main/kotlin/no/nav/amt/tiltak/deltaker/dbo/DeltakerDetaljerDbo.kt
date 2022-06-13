package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerDetaljerDbo(
	val deltakerId: UUID,
	val brukerId: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val telefonnummer: String?,
	val epost: String?,
	val veilederNavn: String?,
	val veilederTelefonnummer: String?,
	val veilederEpost: String?,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	var status: Deltaker.Status,
	var statusId: UUID,
	val statusOpprettet: LocalDateTime,
	val statusGyldigFra: LocalDateTime,
	val navEnhetId: UUID?,
	val navEnhetEnhetId: String?,
	val navEnhetNavn: String?,
	val gjennomforingId: UUID,
	val gjennomforingNavn: String,
	val gjennomforingStartDato: LocalDate?,
	val gjennomforingSluttDato: LocalDate?,
	val gjennomforingStatus: Gjennomforing.Status?,
	val tiltakNavn: String,
	val tiltakKode: String,
	val virksomhetNavn: String,
	val organisasjonNavn: String?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?
) {
	fun toDeltaker() = Deltaker(
		id = deltakerId,
			gjennomforingId = gjennomforingId,
			bruker = Bruker(
				id = brukerId,
				fornavn = fornavn,
				mellomnavn = mellomnavn,
				etternavn = etternavn,
				fodselsnummer = fodselsnummer,
				navEnhet = navEnhetId?.let {
					NavEnhet(
						id = it,
						enhetId = navEnhetEnhetId!!,
						navn = navEnhetNavn!!
					)
				}
			),
			startDato = startDato,
			sluttDato = sluttDato,
			status = DeltakerStatus(
				id = statusId,
				type = status,
				gyldigFra = statusGyldigFra,
				opprettetDato = statusOpprettet,
				aktiv = true,
			),
			registrertDato = registrertDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling
	)
}
