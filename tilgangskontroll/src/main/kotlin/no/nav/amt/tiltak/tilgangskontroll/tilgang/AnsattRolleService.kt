package no.nav.amt.tiltak.tilgangskontroll.tilgang

import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class AnsattRolleService(
	private val ansattRolleRepository: AnsattRolleRepository
) {
	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	fun opprettRolle(id: UUID, ansattId: UUID, arrangorId: UUID, rolle: AnsattRolle) {
		ansattRolleRepository.opprettRolle(id, ansattId, arrangorId, rolle, ZonedDateTime.now(), defaultGyldigTil)
	}

	fun hentAktiveRoller(ansattId: UUID): List<AnsattRolleDbo> {
		return ansattRolleRepository.hentAktiveRoller(ansattId)
	}

	fun deaktiverRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: AnsattRolle) {
		ansattRolleRepository.deaktiverRolleHosArrangor(ansattId, arrangorId, rolle)
	}

	fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		return ansattRolleRepository.hentAktiveRoller(ansattId).distinctBy { it.arrangorId }.map { it.arrangorId }
	}

}
