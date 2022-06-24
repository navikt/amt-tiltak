package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

interface BrukerService {

	fun getBruker(fodselsnummer: String): Bruker?

	fun getOrCreate(fodselsnummer: String): UUID

	fun finnesBruker(fodselsnummer: String): Boolean

	fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID)

	fun oppdaterNavEnhet(fodselsnummer: String, navEnhet: NavEnhet?)

}
