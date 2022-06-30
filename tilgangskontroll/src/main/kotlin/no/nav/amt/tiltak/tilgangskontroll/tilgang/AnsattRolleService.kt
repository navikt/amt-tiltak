package no.nav.amt.tiltak.tilgangskontroll.tilgang

import org.springframework.stereotype.Service
import java.util.*

@Service
class AnsattRolleService(
	private val ansattRolleRepository: AnsattRolleRepository
) {

	fun opprettRolleHvisIkkeFinnes(ansattId: UUID, arrangorId: UUID, rolle: AnsattRolle) {
		// Kan også bli løst med ON CONFLICT, men trenger index

		val roller = ansattRolleRepository.hentRoller(ansattId, arrangorId)

		val rolleFinnes = roller.any { it.rolle == rolle }

		if (!rolleFinnes) {
			ansattRolleRepository.opprettRolle(UUID.randomUUID(), ansattId, arrangorId, rolle)
		}
	}

}
