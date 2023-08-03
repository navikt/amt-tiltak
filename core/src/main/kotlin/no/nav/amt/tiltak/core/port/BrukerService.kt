package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import java.util.UUID

interface BrukerService {
	fun getIdOrCreate(fodselsnummer: String): UUID
	fun slettBruker(personIdent: String)
	fun slettBruker(id: UUID)
	fun upsert(bruker: Bruker)
}
