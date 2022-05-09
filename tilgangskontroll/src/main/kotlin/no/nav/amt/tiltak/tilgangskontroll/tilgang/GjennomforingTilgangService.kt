package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattGjennomforingTilgang
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
open class GjennomforingTilgangService(
	private val gjennomforingTilgangRepository: GjennomforingTilgangRepository
) {

	open fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, opprettetAvNavAnsattId: UUID, gjennomforingId: UUID) {
		gjennomforingTilgangRepository.opprettTilgang(id, arrangorAnsattId, opprettetAvNavAnsattId, gjennomforingId)
	}

	open fun hentTilgang(id: UUID): ArrangorAnsattGjennomforingTilgang {
		return mapGjennomforingTilgang(gjennomforingTilgangRepository.get(id))
	}

	open fun stopTilgang(id: UUID, ansattId: UUID) {
		gjennomforingTilgangRepository.stopTilgang(id, ansattId, ZonedDateTime.now())
	}

	private fun mapGjennomforingTilgang(dbo: GjennomforingTilgangDbo): ArrangorAnsattGjennomforingTilgang {
		return ArrangorAnsattGjennomforingTilgang(
			id = dbo.id,
			ansattId = dbo.ansattId,
			gjennomforingId = dbo.gjennomforingId,
			createdAt = dbo.createdAt
		)
	}

}
