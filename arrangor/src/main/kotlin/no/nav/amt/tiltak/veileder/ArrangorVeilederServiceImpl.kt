package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class ArrangorVeilederServiceImpl (
	private val arrangorVeilederRepository: ArrangorVeilederRepository,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
): ArrangorVeilederService {

	private val maksMedveiledere = 3

	override fun opprettVeiledere(veilederInputs: List<ArrangorVeilederInput>, deltakerIder: List<UUID>) {
		if (veilederInputs.filter { !it.erMedveileder }.size > 1) {
			throw ValidationException("Deltakere kan kun ha 1 veileder.")
		}

		verifiserVeilederTilganger(deltakerIder, veilederInputs.map { it.ansattId })

		val veiledere = veilederInputs.map { OpprettVeilederDbo(it.ansattId, it.erMedveileder) }

		inaktiverVeiledereSomSkalErstattes(veiledere, deltakerIder)

		arrangorVeilederRepository.opprettVeiledere(veiledere, deltakerIder)
	}

	override fun hentVeiledereForDeltaker(deltakerId: UUID): List<ArrangorVeileder> {
		return arrangorVeilederRepository.getAktiveForDeltaker(deltakerId).map { it.toArrangorVeileder() }
	}

	private fun inaktiverVeiledereSomSkalErstattes(veiledere: List<OpprettVeilederDbo>, deltakerIder: List<UUID>) {
		val antallNyeMedveiledere = veiledere.count { it.erMedveileder }

		if (antallNyeMedveiledere > maksMedveiledere)
			throw ValidationException("Kan ikke håndtere flere enn $maksMedveiledere medveiledere")

		arrangorVeilederRepository.inaktiverVeiledereForDeltakere(
			veiledere.map { it.ansattId },
			deltakerIder,
		)

		val aktiveVeiledere = arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder)

		val veiledereSomSkalErstattes = aktiveVeiledereSomSkalErstattes(veiledere, aktiveVeiledere)
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
			.flatMap { (_, medveiledere) ->
				val forsteVeilederSomSkalInaktiveres = maksMedveiledere - antallNyeMedveiledere
				medveiledere
					.sortedByDescending { it.gyldigFra }
					.slice(forsteVeilederSomSkalInaktiveres until medveiledere.size)
					.map { it.id }
			}

	}

	private fun aktiveVeiledereSomSkalErstattes(
		veiledere: List<OpprettVeilederDbo>,
		aktiveVeiledere: List<ArrangorVeilederDbo>,
	) : List<UUID> {
		if (veiledere.any { !it.erMedveileder }) {
			return aktiveVeiledere.filter { !it.erMedveileder }.map { it.id }
		}
		return emptyList()

	}

	private fun verifiserVeilederTilganger(deltakerIder: List<UUID>, veilederIder: List<UUID>) {
		val gjennomforingIder = deltakerService.hentDeltakere(deltakerIder)
			.map { it.gjennomforingId }
			.toSet()
			.toList()

		val arrangorIder = gjennomforingService.getGjennomforinger(gjennomforingIder).map { it.arrangor.id }

		arrangorAnsattTilgangService.verifiserAnsatteHarRolleHosArrangorer(
			veilederIder,
			arrangorIder,
			ArrangorAnsattRolle.VEILEDER,
		)
	}

}

internal data class OpprettVeilederDbo(
	val ansattId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime = ZonedDateTime.now(),
	val gyldigTil: ZonedDateTime = ZonedDateTime.parse("3000-01-01T00:00Z"),
)
