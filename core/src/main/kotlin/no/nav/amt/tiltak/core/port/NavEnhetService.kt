package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.UUID

interface NavEnhetService {

	fun getNavEnhet(id: UUID): NavEnhet

	fun getNavEnhet(enhetId: String): NavEnhet?

	fun getNavEnhetForBruker(personIdent: String): NavEnhet?

	fun upsert(enhet: NavEnhet)

}

