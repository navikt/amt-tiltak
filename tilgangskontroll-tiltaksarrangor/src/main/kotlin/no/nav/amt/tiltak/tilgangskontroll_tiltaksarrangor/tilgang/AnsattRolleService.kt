package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class AnsattRolleService(
	private val ansattRolleRepository: AnsattRolleRepository,
	private val publisherService: DataPublisherService
) {
	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	fun opprettRolle(id: UUID, ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		ansattRolleRepository.opprettRolle(id, ansattId, arrangorId, rolle, ZonedDateTime.now(), defaultGyldigTil)
			.also { publisherService.publish(ansattId, DataPublishType.ARRANGOR_ANSATT) }
	}

	fun hentAktiveRoller(ansattId: UUID): List<ArrangorAnsattRoller> {
		val aktiveRoller = ansattRolleRepository.hentAktiveRoller(ansattId)

		val rolleMap = mutableMapOf<UUID, MutableList<ArrangorAnsattRolle>>()

		aktiveRoller.forEach {
			val roller = rolleMap.computeIfAbsent(it.arrangorId) { mutableListOf() }
			roller.add(it.rolle)
			rolleMap[it.arrangorId] = roller
		}

		return rolleMap.map { ArrangorAnsattRoller(it.key, it.value) }
	}

	fun deaktiverRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		ansattRolleRepository.deaktiverRolleHosArrangor(ansattId, arrangorId, rolle)
			.also { publisherService.publish(ansattId, DataPublishType.ARRANGOR_ANSATT) }
	}

	fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		return ansattRolleRepository.hentAktiveRoller(ansattId).distinctBy { it.arrangorId }.map { it.arrangorId }
	}

}
