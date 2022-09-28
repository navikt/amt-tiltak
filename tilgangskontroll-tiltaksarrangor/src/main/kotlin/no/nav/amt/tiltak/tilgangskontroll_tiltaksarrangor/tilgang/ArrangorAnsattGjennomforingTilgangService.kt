package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.port.GjennomforingService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.*

@Service
open class ArrangorAnsattGjennomforingTilgangService(
	private val arrangorAnsattGjennomforingTilgangRepository: ArrangorAnsattGjennomforingTilgangRepository,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate
) {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	open fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID) {
		val harAlleredeTilgang = hentGjennomforingerForAnsatt(arrangorAnsattId)
			.contains(gjennomforingId)

		if (harAlleredeTilgang) {
			throw IllegalStateException(
				"Kan ikke opprette tilgang på gjennomføring siden arrangør ansatt allerede har tilgang"
			)
		}

		arrangorAnsattGjennomforingTilgangRepository.opprettTilgang(
			id = id,
			arrangorAnsattId = arrangorAnsattId,
			gjennomforingId = gjennomforingId,
			gyldigFra = ZonedDateTime.now(),
			gyldigTil = defaultGyldigTil
		)

	}

	open fun fjernTilgang(arrangorAnsattId: UUID, gjennomforingId: UUID) {
		arrangorAnsattGjennomforingTilgangRepository.fjernTilgang(arrangorAnsattId, gjennomforingId)
	}

	fun fjernTilgangTilGjennomforinger(arrangorAnsattId: UUID, arrangorId: UUID) {
		val tilganger = arrangorAnsattGjennomforingTilgangRepository
			.hentAktiveGjennomforingTilgangerForAnsatt(arrangorAnsattId)
		val gjennomforinger = gjennomforingService.getByArrangorId(arrangorId)

		transactionTemplate.executeWithoutResult {
			tilganger
				.filter { tilgang -> gjennomforinger.any { tilgang.gjennomforingId == it.id } }
				.forEach { fjernTilgang(arrangorAnsattId, it.gjennomforingId) }
		}
	}

	fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return arrangorAnsattGjennomforingTilgangRepository.hentAktiveGjennomforingTilgangerForAnsatt(ansattId)
			.map { it.gjennomforingId }
	}

}
