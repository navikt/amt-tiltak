package no.nav.amt.tiltak.tilgangskontroll.tilgang

import org.springframework.stereotype.Service
import java.util.*

@Service
class GjennomforingTilgangService(
	private val gjennomforingTilgangRepository: GjennomforingTilgangRepository
) {

	fun opprettTilgang(id: UUID, ansattId: UUID, gjennomforingId: UUID) {
		gjennomforingTilgangRepository.opprettTilgang(id, ansattId, gjennomforingId)
	}

}
