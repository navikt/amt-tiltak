package no.nav.amt.tiltak.tilgangskontroll.tiltaksansvarlig_tilgang

import no.nav.amt.tiltak.core.domain.tilgangskontroll.TiltaksansvarligGjennomforingTilgang
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@Service
open class TiltaksansvarligTilgangServiceImpl(
	private val navAnsattService: NavAnsattService,
	private val tiltaksansvarligGjennomforingTilgangRepository: TiltaksansvarligGjennomforingTilgangRepository
) : TiltaksansvarligTilgangService {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	override fun harTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID): Boolean {
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		return hentAktiveTilganger(navAnsatt.id)
			.any { it.gjennomforingId == gjennomforingId }
	}

	override fun giTilgangTilGjennomforing(navAnsattId: UUID, gjennomforingId: UUID) {
		val tilganger = hentAktiveTilganger(navAnsattId)

		if (tilganger.any { it.gjennomforingId == gjennomforingId }) {
			// Kast heller custom exception og map
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Har allerede tilgang til gjennomføring")
		}

		tiltaksansvarligGjennomforingTilgangRepository.opprettTilgang(
			id = UUID.randomUUID(),
			navAnsattId = navAnsattId,
			gjennomforingId = gjennomforingId,
			gyldigTil = defaultGyldigTil
		)
	}

	override fun stopTilgangTilGjennomforing(navAnsattId: UUID, gjennomforingId: UUID) {
		val tilganger = hentAktiveTilganger(navAnsattId)

		// Kast heller custom exception og map

		val tilgang = tilganger.find { it.gjennomforingId == gjennomforingId }
			?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Tilgang finnes ikke eller er allerede stoppet")

		tiltaksansvarligGjennomforingTilgangRepository.stopTilgang(tilgang.id)
	}

	override fun hentAktiveTilganger(navAnsattId: UUID): List<TiltaksansvarligGjennomforingTilgang> {
		val tilganger = tiltaksansvarligGjennomforingTilgangRepository.hentAktiveTilganger(navAnsattId)

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

	override fun hentAktiveTilganger(navIdent: String): List<TiltaksansvarligGjennomforingTilgang> {
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)
		return hentAktiveTilganger(navAnsatt.id)
	}

}
