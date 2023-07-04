package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.MineDeltakerlisterService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.UUID

@Service
open class MineDeltakerlisterServiceImpl(
	private val mineDeltakerlisterRepository: MineDeltakerlisterRepository,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate,
	private val publisherService: DataPublisherService
): MineDeltakerlisterService {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	override fun leggTil(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID) {
		val harAlleredeTilgang = hent(arrangorAnsattId)
			.contains(gjennomforingId)

		if (harAlleredeTilgang) {
			throw IllegalStateException(
				"Kan ikke opprette tilgang på gjennomføring siden arrangør ansatt allerede har tilgang"
			)
		}

		mineDeltakerlisterRepository.leggTil(
			id = id,
			arrangorAnsattId = arrangorAnsattId,
			gjennomforingId = gjennomforingId,
			gyldigFra = ZonedDateTime.now(),
			gyldigTil = defaultGyldigTil
		)
	}

	override fun fjern(arrangorAnsattId: UUID, gjennomforingId: UUID) {
		mineDeltakerlisterRepository.fjern(arrangorAnsattId, gjennomforingId)
	}

	override fun fjernAlleHosArrangor(arrangorAnsattId: UUID, arrangorId: UUID) {
		val tilganger = mineDeltakerlisterRepository
			.hent(arrangorAnsattId)
		val gjennomforinger = gjennomforingService.getByArrangorId(arrangorId)

		transactionTemplate.executeWithoutResult {
			tilganger
				.filter { tilgang -> gjennomforinger.any { tilgang.gjennomforingId == it.id } }
				.forEach { fjern(arrangorAnsattId, it.gjennomforingId) }
		}
	}

	override fun hent(ansattId: UUID): List<UUID> {
		return mineDeltakerlisterRepository.hent(ansattId)
			.map { it.gjennomforingId }
	}

	override fun erLagtTil(ansattId: UUID, gjennomforingId: UUID): Boolean {
		val gjennomforinger = hent(ansattId)
		return gjennomforinger.contains(gjennomforingId)
	}

}
