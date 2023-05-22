package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.IdentType
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

interface BrukerService {
	fun updateBrukerByPersonIdent(personIdent: String, fornavn: String, mellomnavn: String?, etternavn: String)

	fun updateBrukerByPersonIdent(brukerId: UUID): Boolean

	fun updateAllBrukere()

	fun logSkjermedeBrukere()

	fun oppdaterNavEnhet(personIdent: String, navEnhet: NavEnhet?)

	fun erSkjermet(personIdent: String): Boolean

	fun settErSkjermet(personIdent: String, erSkjermet: Boolean)

	fun getOrCreate(fodselsnummer: String): UUID

	fun finnesBruker(personIdent: String): Boolean

	fun oppdaterAnsvarligVeileder(personIdent: String, navAnsattId: UUID)

	fun oppdaterPersonIdenter(gjeldendeIdent: String, identType: IdentType, historiskeIdenter: List<String>)

	fun slettBruker(personIdent: String)
	fun migrerAlle()
}
