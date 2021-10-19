package no.nav.amt.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.springframework.stereotype.Service

@Service
class TiltakServiceImpl(
	val tiltakRepository: TiltakRepository
) : TiltakService {

	override fun addTiltak(unsavedTiltak: Tiltak): Tiltak {
		if (unsavedTiltak.id != null) {
			throw UnsupportedOperationException(
				"Kan ikke legge til et tiltak som allerede har en id " +
					"(${unsavedTiltak.id})"
			)
		}

		return tiltakRepository.insert(unsavedTiltak).toTiltak(unsavedTiltak.tiltaksleverandorId)
	}


}
