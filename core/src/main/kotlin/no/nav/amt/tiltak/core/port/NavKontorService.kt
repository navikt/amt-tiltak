package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import java.util.*

interface NavKontorService {

	fun hentNavKontorer(enhetIder: List<String>): List<NavKontor>

	fun getNavKontorForBruker(fodselsnummer: String): NavKontor?

	fun getNavKontor(enhetId: String): NavKontor

	fun getNavKontor(id: UUID): NavKontor

}

