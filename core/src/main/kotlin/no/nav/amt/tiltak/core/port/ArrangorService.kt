package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.util.UUID

interface ArrangorService {

	fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor?

	fun getArrangorById(id: UUID): Arrangor

	fun getArrangorerById(arrangorIder: List<UUID>): List<Arrangor>

	fun getOrCreateArrangor(arrangor: Arrangor): Arrangor

	fun getOrCreateArrangorByOrgnr(organisasjonsnummer: String): Arrangor
	fun updateArrangor(arrangor: Arrangor): Arrangor
}
