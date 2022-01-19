package no.nav.amt.tiltak.core.port

import java.util.*

interface BrukerService {

	fun getOrCreate(fodselsnummer: String): UUID

	fun finnesBruker(fodselsnummer: String): Boolean

	fun oppdaterAnsvarligVeileder(brukerPersonligIdent: String, veilederId: UUID)

}
