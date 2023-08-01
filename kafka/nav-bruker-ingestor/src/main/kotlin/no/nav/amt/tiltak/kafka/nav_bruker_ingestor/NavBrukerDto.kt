package no.nav.amt.tiltak.kafka.nav_bruker_ingestor

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.UUID

data class NavBrukerDto(
	val personId: UUID,
	val personident: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val navVeilederId: UUID?,
	val navEnhet: NavEnhetDto?,
	val telefon: String?,
	val epost: String?,
	val erSkjermet: Boolean,
) {
	fun toModel() = Bruker(
		id = personId,
		personIdent = personident,
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn =  etternavn,
		navEnhetId = navEnhet?.id,
		ansvarligVeilederId = navVeilederId,
		telefonnummer = telefon,
		epost = epost,
		erSkjermet = erSkjermet,
		// Følgende kommer ikke på topic og bør fjernes fra domenet på sikt
		personIdentType = null,
		historiskeIdenter = emptyList(),
	)
}

data class NavEnhetDto(
	val id: UUID,
	val enhetId: String,
	val navn: String,
) {
	fun toModel() = NavEnhet(id, enhetId, navn)
}
