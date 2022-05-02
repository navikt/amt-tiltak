package no.nav.amt.tiltak.tiltaksoversikt

import no.nav.amt.tiltak.core.domain.tilgangskontroll.TiltaksansvarligGjennomforingTilgang
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
open class TiltaksoversiktService(
	private val tiltaksansvarligGjennomforingTilgangRepository: TiltaksansvarligGjennomforingTilgangRepository
) {

	open fun hentAktiveTilgangerForTiltaksansvarlig(navAnsattId: UUID): List<TiltaksansvarligGjennomforingTilgang> {
		val tilganger = tiltaksansvarligGjennomforingTilgangRepository.hentAktiveTilgangerTilTiltaksansvarlig(navAnsattId)

		return tilganger.map {
			TiltaksansvarligGjennomforingTilgang(
				id = it.id,
				navAnsattId = it.navAnsattId,
				gjennomforingId = it.gjennomforingId,
				gyldigTil = it.gyldigTil,
				createdAt = it.createdAt,
			)
		}
	}

	open fun leggTilGjennomforingIOversikt(navAnsattId: UUID, gjennomforingId: UUID) {
		val tilganger = hentAktiveTilgangerForTiltaksansvarlig(navAnsattId)

		if (tilganger.any { it.gjennomforingId == gjennomforingId }) {
			// Kast heller custom exception og map
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Har allerede tilgang til gjennomføring")
		}

		tiltaksansvarligGjennomforingTilgangRepository.opprettTilgang(
			id = UUID.randomUUID(),
			navAnsattId = navAnsattId,
			gjennomforingId = gjennomforingId
		)
	}

	open fun fjernGjennomforingFraOversikt(navAnsattId: UUID, gjennomforingId: UUID) {
		val tilganger = hentAktiveTilgangerForTiltaksansvarlig(navAnsattId)

		// Kast heller custom exception og map

		val tilgang = tilganger.find { it.gjennomforingId == gjennomforingId }
			?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Tilgang finnes ikke eller er allerede stoppet")

		tiltaksansvarligGjennomforingTilgangRepository.stopTilgang(tilgang.id)
	}

}
