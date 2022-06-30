package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

interface NavEnhetService {

	fun getNavEnhet(id: UUID): NavEnhet

	fun getNavEnhet(enhetId: String): NavEnhet?

	fun getNavEnhetForBruker(fodselsnummer: String): NavEnhet?

}

