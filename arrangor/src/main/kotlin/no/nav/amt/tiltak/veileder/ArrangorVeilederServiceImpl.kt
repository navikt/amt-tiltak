package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.UUID

@Service
class ArrangorVeilederServiceImpl (
	private val arrangorVeilederRepository: ArrangorVeilederRepository,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate
): ArrangorVeilederService {

	private val log = LoggerFactory.getLogger(javaClass)
	private val maksMedveiledere = 3
	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00Z")

	override fun opprettVeiledereForDeltaker(veilederInputs: List<ArrangorVeilederInput>, deltakerId: UUID) {
		val veiledere = mapOpprettVeilederDboer(veilederInputs)

		if (veiledere.count { it.erMedveileder } > maksMedveiledere)
			throw ValidationException("Deltakere kan ikke ha flere enn $maksMedveiledere medveiledere")

		transactionTemplate.executeWithoutResult {
			arrangorVeilederRepository.inaktiverAlleVeiledereForDeltaker(deltakerId)
			arrangorVeilederRepository.opprettVeiledere(veiledere, deltakerId)
		}
	}

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
		if (deltakerService.hentDeltaker(deltakerId) == null) {
			log.warn("Deltaker med id $deltakerId finnes ikke, kan ikke sette ansatt $ansattId som veileder")
		} else {
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
	}

	override fun fjernAnsattSomVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean) {
		arrangorVeilederRepository.inaktiverVeileder(ansattId = ansattId, deltakerId = deltakerId, erMedveileder = erMedveileder)
	}

	private fun mapOpprettVeilederDboer(veilederInputs: List<ArrangorVeilederInput>): List<OpprettVeilederDbo> {
		if (veilederInputs.filter { !it.erMedveileder }.size > 1) {
			throw ValidationException("Deltakere kan kun ha 1 veileder.")
		}

		return veilederInputs.map {
			OpprettVeilederDbo(
				ansattId = it.ansattId,
				erMedveileder = it.erMedveileder,
				gyldigFra = ZonedDateTime.now(),
				gyldigTil = defaultGyldigTil,
			)
		}
	}
}

internal data class OpprettVeilederDbo(
	val ansattId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
)
