package no.nav.amt.tiltak.tilgangskontroll.tilgang

import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class AnsattRolleService(
	private val ansattRolleRepository: AnsattRolleRepository
) {
	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	fun opprettRolleHvisIkkeFinnes(ansattId: UUID, arrangorId: UUID, rolle: AnsattRolle) {
		// Kan også bli løst med ON CONFLICT, men trenger index

		val roller = ansattRolleRepository.hentAktiveRoller(ansattId, arrangorId)

		val rolleFinnes = roller.any { it.rolle == rolle }

		if (!rolleFinnes) {
			ansattRolleRepository.opprettRolle(UUID.randomUUID(), ansattId, arrangorId, rolle, ZonedDateTime.now(), defaultGyldigTil)
		}
	}

}
