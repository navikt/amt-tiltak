package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID

@Service
class ArrangorVeilederServiceImpl (
	private val arrangorVeilederRepository: ArrangorVeilederRepository,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService
): ArrangorVeilederService {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00Z")

	override fun hentVeiledereForDeltaker(deltakerId: UUID): List<ArrangorVeileder> {
		return arrangorVeilederRepository.getAktiveForDeltaker(deltakerId).map { it.toArrangorVeileder() }
	}

	override fun hentDeltakereForVeileder(ansattId: UUID): List<ArrangorVeileder> {
		return arrangorVeilederRepository.getDeltakereForVeileder(ansattId).map { it.toArrangorVeileder() }
	}

	override fun erVeilederFor(ansattId: UUID, deltakerId: UUID): Boolean {
		return hentVeiledereForDeltaker(deltakerId).any { it.ansattId == ansattId }
	}

	override fun fjernAlleDeltakereForVeilederHosArrangor(ansattId: UUID, arrangorId: UUID) {
		val gjennomforingIder = gjennomforingService.getByArrangorId(arrangorId)
			.map { it.id }
			.distinct()

		if (gjennomforingIder.isNotEmpty()) {
			arrangorVeilederRepository.inaktiverVeilederPaGjennomforinger(ansattId, gjennomforingIder)
		}
	}

	override fun leggTilAnsattSomVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean) {
		if (deltakerService.hentDeltaker(deltakerId) == null) return

		arrangorVeilederRepository.lagreVeileder(
			deltakerId = deltakerId,
			opprettVeilederDbo = OpprettVeilederDbo(
				ansattId = ansattId,
				erMedveileder = erMedveileder,
				gyldigFra = ZonedDateTime.now(),
				gyldigTil = defaultGyldigTil
			)
		)

	}

	override fun fjernAnsattSomVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean) {
		arrangorVeilederRepository.inaktiverVeileder(
			ansattId = ansattId,
			deltakerId = deltakerId,
			erMedveileder = erMedveileder
		)
	}
}

internal data class OpprettVeilederDbo(
	val ansattId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
)
