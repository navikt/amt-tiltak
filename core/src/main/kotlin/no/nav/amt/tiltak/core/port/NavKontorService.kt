package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor

interface NavKontorService {

	fun hentNavKontorer(enhetIder: List<String>): List<NavKontor>

	fun upsertNavKontor(enhetId: String, navn: String)

}

