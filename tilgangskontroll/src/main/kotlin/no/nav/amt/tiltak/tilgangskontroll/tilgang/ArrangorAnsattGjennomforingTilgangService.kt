package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattGjennomforingTilgang
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
open class ArrangorAnsattGjennomforingTilgangService(
	private val arrangorAnsattGjennomforingTilgangRepository: ArrangorAnsattGjennomforingTilgangRepository
) {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	open fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID) {
		arrangorAnsattGjennomforingTilgangRepository.opprettTilgang(
			id = id,
			arrangorAnsattId = arrangorAnsattId,
			gjennomforingId = gjennomforingId,
			gyldigFra = ZonedDateTime.now(),
			gyldigTil = defaultGyldigTil
		)
	}

	open fun hentTilgang(id: UUID): ArrangorAnsattGjennomforingTilgang {
		return mapGjennomforingTilgang(arrangorAnsattGjennomforingTilgangRepository.get(id))
	}

	open fun stopTilgang(id: UUID) {
		arrangorAnsattGjennomforingTilgangRepository.oppdaterGyldigTil(id, ZonedDateTime.now())
	}

	private fun mapGjennomforingTilgang(dbo: ArrangorAnsattGjennomforingTilgangDbo): ArrangorAnsattGjennomforingTilgang {
		return ArrangorAnsattGjennomforingTilgang(
			id = dbo.id,
			ansattId = dbo.ansattId,
			gjennomforingId = dbo.gjennomforingId,
			createdAt = dbo.createdAt
		)
	}

}
