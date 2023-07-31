package no.nav.amt.tiltak.clients.amt_person.model

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.UUID

data class NavBruker(
	val personId: UUID,
	val personident: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val navVeilederId: UUID?,
	val navEnhet: NavEnhet?,
	val telefon: String?,
	val epost: String?,
	val erSkjermet: Boolean,
) {
	fun toBruker() = Bruker(
		id = personId,
		personIdent = personident,
		personIdentType = null,
		historiskeIdenter = emptyList(),
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		telefonnummer = telefon,
		epost = epost,
		ansvarligVeilederId = navVeilederId,
		navEnhetId = navEnhet?.id,
		erSkjermet = erSkjermet,
	)


}
