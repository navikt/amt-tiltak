package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import java.util.*

interface BrukerService {

	fun getBruker(fodselsnummer: String): Bruker?

	fun getOrCreate(fodselsnummer: String): UUID

	fun finnesBruker(fodselsnummer: String): Boolean

	fun oppdaterAnsvarligVeileder(brukerPersonligIdent: String, veilederId: UUID)

	fun oppdaterNavKontor(fodselsnummer: String, navKontor: NavKontor)

}
