package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.MineDeltakerlisterService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.*

@Service
open class MineDeltakerlisterServiceImpl(
	private val mineDeltakerlisterRepository: MineDeltakerlisterRepository,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate
): MineDeltakerlisterService {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	override fun leggTil(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID) {
		val harAlleredeTilgang = hentAlleForAnsatt(arrangorAnsattId)
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

	override fun fjernGjennomforinger(arrangorAnsattId: UUID, arrangorId: UUID) {
		val tilganger = mineDeltakerlisterRepository
			.hent(arrangorAnsattId)
		val gjennomforinger = gjennomforingService.getByArrangorId(arrangorId)

		transactionTemplate.executeWithoutResult {
			tilganger
				.filter { tilgang -> gjennomforinger.any { tilgang.gjennomforingId == it.id } }
				.forEach { fjern(arrangorAnsattId, it.gjennomforingId) }
		}
	}

	override fun hentAlleForAnsatt(ansattId: UUID): List<UUID> {
		return mineDeltakerlisterRepository.hent(ansattId)
			.map { it.gjennomforingId }
	}

	override fun harLagtTilDeltakerliste(ansattId: UUID, gjennomforingId: UUID): Boolean {
		val gjennomforinger = hentAlleForAnsatt(ansattId)
		return gjennomforinger.contains(gjennomforingId)
	}

}
