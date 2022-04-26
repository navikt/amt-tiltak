package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

interface NavEnhetService {

	fun hentNavEnheter(enhetIder: List<String>): List<NavEnhet>

	fun upsertNavEnhet(enhetId: String, navn: String)

	fun getNavEnhetForBruker(fodselsnummer: String): NavEnhet?

	fun getNavEnhet(enhetId: String): NavEnhet

	fun getNavEnhet(id: UUID): NavEnhet

}

