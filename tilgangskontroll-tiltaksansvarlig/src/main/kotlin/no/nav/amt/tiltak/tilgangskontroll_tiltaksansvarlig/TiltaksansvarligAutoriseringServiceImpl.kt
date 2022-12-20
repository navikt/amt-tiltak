package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig

import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGruppeService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGrupper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class TiltaksansvarligAutoriseringServiceImpl(
	private val adGruppeService: AdGruppeService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService
) : TiltaksansvarligAutoriseringService {

	override fun verifiserTilgangTilFlate(navAnsattAzureId: UUID) {
		val harTilgang = adGruppeService.hentAdGrupper(navAnsattAzureId)
			.any { it.name == AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE }

		if (!harTilgang) {
			secureLog.warn(
				"""
					$navAnsattAzureId har ikke tilgang til tiltaksansvarlig flate. Er ikke medlem av
					${AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE}
				""".trimIndent()
			)

			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Mangler tilgang til AD-gruppe")
		}
	}

	override fun verifiserTilgangTilEndringsmelding(navAnsattAzureId: UUID) {
		val harTilgang = adGruppeService.hentAdGrupper(navAnsattAzureId)
			.any { it.name == AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE }

		if (!harTilgang) {
			secureLog.warn(
				"""
					$navAnsattAzureId har ikke tilgang til endringsmeldinger. Er ikke medlem av
					${AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE}
				""".trimIndent()
			)

			throw UnauthorizedException("Mangler tilgang til AD-gruppe")
		}
	}

	override fun verifiserTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID) {
		val harTilgang = tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)

		if (!harTilgang) {
			secureLog.warn("$navIdent har ikke tilgang til gjennomføring med id=$gjennomforingId")

			throw UnauthorizedException("Ikke tilgang til gjennomføring")
		}
	}

	override fun harTilgangTilSkjermedePersoner(navAnsattAzureId: UUID) : Boolean {
		return adGruppeService.hentAdGrupper(navAnsattAzureId)
			.any { it.id == AdGrupper.EGEN_ANSATT_GRUPPE_ID }

	}

}
