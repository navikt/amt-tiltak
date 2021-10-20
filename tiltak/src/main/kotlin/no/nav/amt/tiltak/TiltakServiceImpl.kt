package no.nav.amt.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltaksinstansRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TiltakServiceImpl(
	private val tiltakRepository: TiltakRepository,
	private val tiltaksinstansRepository: TiltaksinstansRepository
) : TiltakService {


	override fun getTiltakFromArenaId(arenaId: String): Tiltak? {
		return tiltakRepository.getByArenaId(arenaId)?.toTiltak()
	}

	override fun addTiltak(unsavedTiltak: Tiltak): Tiltak {
		if (unsavedTiltak.id != null) {
			throw UnsupportedOperationException(
				"Kan ikke legge til et tiltak som allerede har en id " +
					"(${unsavedTiltak.id})"
			)
		}

		return tiltakRepository.insert("1", unsavedTiltak).toTiltak()
	}

	override fun addTiltaksinstans(arenaId: Int, instans: TiltakInstans): TiltakInstans {
		return tiltaksinstansRepository.insert(arenaId, instans).toTiltaksinstans(instans.tiltakId)
	}


}
