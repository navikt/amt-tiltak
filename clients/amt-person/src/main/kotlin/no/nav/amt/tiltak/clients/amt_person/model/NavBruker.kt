package no.nav.amt.tiltak.clients.amt_person.model

import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
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
	val adresse: Adresse?,
	val adressebeskyttelse: Adressebeskyttelse?
) {
	fun toBruker() = Bruker(
		id = personId,
		personIdent = personident,
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		telefonnummer = telefon,
		epost = epost,
		ansvarligVeilederId = navVeilederId,
		navEnhetId = navEnhet?.id,
		erSkjermet = erSkjermet,
		adresse = adresse,
		adressebeskyttelse = adressebeskyttelse
	)
}
