package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor

interface NavKontorService {

	fun hentNavKontorForBruker(fnr: String): NavKontor?

}

