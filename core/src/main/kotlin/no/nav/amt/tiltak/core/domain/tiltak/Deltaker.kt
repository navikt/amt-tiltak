package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Deltaker(
	val id: UUID = UUID.randomUUID(),
	val gjennomforingId: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val telefonnummer: String?,
	val erSkjermet: Boolean,
	val epost: String?,
	val personIdent: String,
	val navEnhet: NavEnhet?,
	val navVeilederId: UUID?,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatus,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String? = null,
	val endretDato: LocalDateTime,
	val adressebeskyttelse: Adressebeskyttelse?,
	val innhold: DeltakelsesInnhold?,
	val kilde: Kilde
) {
	fun harAdressebeskyttelse(): Boolean {
		return adressebeskyttelse != null
	}
}
