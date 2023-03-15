package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.domain.tiltak.ArrangorVeiledersDeltaker
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.*

@Service
class ArrangorVeilederServiceImpl (
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorVeilederRepository: ArrangorVeilederRepository,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate,
): ArrangorVeilederService {

	private val maksMedveiledere = 3
	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00Z")

	override fun opprettVeiledere(veilederInputs: List<ArrangorVeilederInput>, deltakerIder: List<UUID>) {
		val veiledere = mapOpprettVeilederDboer(veilederInputs)

		transactionTemplate.executeWithoutResult {
			inaktiverVeiledereSomSkalErstattes(veiledere, deltakerIder)
			arrangorVeilederRepository.opprettVeiledere(veiledere, deltakerIder)
		}
	}

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

	override fun hentAktiveVeiledereForGjennomforing(gjennomforingId: UUID): List<ArrangorVeileder> {
		val deltakerIder = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId).map { it.id }

		return arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder).map { it.toArrangorVeileder() }
	}

	override fun hentTilgjengeligeVeiledereForGjennomforing(gjennomforingId: UUID): List<Ansatt> {
		val arrangorId = gjennomforingService.getArrangorId(gjennomforingId)

		// Henter alle veiledere hos arrangør inntil noen form for veileder-gjennomføringtilgang er implementert.
		return arrangorAnsattService.getVeiledereForArrangor(arrangorId)
	}

	override fun erVeilederFor(ansattId: UUID, deltakerId: UUID): Boolean {
		return hentVeiledereForDeltaker(deltakerId).any { it.ansattId == ansattId }
	}

	override fun hentDeltakerliste(ansattId: UUID): List<ArrangorVeiledersDeltaker> {
		return arrangorVeilederRepository.getDeltakerlisteForVeileder(ansattId)
	}

	override fun fjernAlleDeltakereForVeilederHosArrangor(ansattId: UUID, arrangorId: UUID) {
		val gjennomforingIder = gjennomforingService.getByArrangorId(arrangorId)
			.map { it.id }
			.distinct()

		arrangorVeilederRepository.inaktiverVeilederPaGjennomforinger(ansattId, gjennomforingIder)
	}

	private fun inaktiverVeiledereSomSkalErstattes(veiledere: List<OpprettVeilederDbo>, deltakerIder: List<UUID>) {
		val antallNyeMedveiledere = veiledere.count { it.erMedveileder }

		if (antallNyeMedveiledere > maksMedveiledere)
			throw ValidationException("Deltakere kan ikke ha flere enn $maksMedveiledere medveiledere")

		arrangorVeilederRepository.inaktiverVeiledereForDeltakere(
			veiledere.map { it.ansattId },
			deltakerIder,
		)

		val aktiveVeiledere = arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder)

		val veiledereSomSkalErstattes = aktiveHovedveiledereSomSkalErstattes(veiledere, aktiveVeiledere)
			.plus(aktiveMedveiledereSomSkalErstattes(aktiveVeiledere, antallNyeMedveiledere))

		arrangorVeilederRepository.inaktiverVeiledere(veiledereSomSkalErstattes)
	}

	private fun aktiveMedveiledereSomSkalErstattes(
		aktiveVeiledere: List<ArrangorVeilederDbo>,
		antallNyeMedveiledere: Int,
	) : List<UUID> {
		return aktiveVeiledere
			.filter { it.erMedveileder }
			.groupBy { it.deltakerId }
			.filterValues { medveiledere -> medveiledere.size + antallNyeMedveiledere > maksMedveiledere }
			.flatMap { (_, medveiledere) -> finnMedveiledereSomSkalErstattes(medveiledere, antallNyeMedveiledere) }
	}

	private fun finnMedveiledereSomSkalErstattes(
		medveiledere: List<ArrangorVeilederDbo>,
		antallNyeMedveiledere: Int,
	): List<UUID> {
		val forsteVeilederSomSkalInaktiveres = maksMedveiledere - antallNyeMedveiledere
		return medveiledere
			.sortedByDescending { it.gyldigFra }
			.slice(forsteVeilederSomSkalInaktiveres until medveiledere.size)
			.map { it.id }
	}

	private fun aktiveHovedveiledereSomSkalErstattes(
		veiledere: List<OpprettVeilederDbo>,
		aktiveVeiledere: List<ArrangorVeilederDbo>,
	) : List<UUID> {
		if (veiledere.any { !it.erMedveileder }) {
			return aktiveVeiledere.filter { !it.erMedveileder }.map { it.id }
		}
		return emptyList()
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
