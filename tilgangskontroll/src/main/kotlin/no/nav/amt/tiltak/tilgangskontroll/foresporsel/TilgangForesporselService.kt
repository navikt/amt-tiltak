package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import org.springframework.stereotype.Service
import java.util.*

@Service
class TilgangForesporselService(
	private val tilgangForesporselRepository: TilgangForesporselRepository
) {

	fun hentUbesluttedeForesporsler(gjennomforingId: UUID): List<TilgangForesporselDbo> {
		return tilgangForesporselRepository.hentUbesluttedeForesporsler(gjennomforingId)
	}

	fun opprettForesporsel(opprettForesporselCmd: OpprettForesporselCmd) {
		tilgangForesporselRepository.opprettForesporsel(opprettForesporselCmd)
	}

	fun godkjennForesporsel(foresporselId: UUID, godkjentAvNavAnsattId: UUID) {
		val foresporsel = tilgangForesporselRepository.hentForesporsel(foresporselId)

		val nyGjennomforingTilgangId = UUID.randomUUID()

		// Upsert tilgang til arrangor
		// Opprett ny tilgang

		tilgangForesporselRepository.godkjennForesporsel(foresporselId, godkjentAvNavAnsattId, nyGjennomforingTilgangId)
	}

	fun avvisForesporsel(foresporselId: UUID, avvistAvNavAnsattId: UUID) {
		tilgangForesporselRepository.avvisForesporsel(foresporselId, avvistAvNavAnsattId)
	}

}
