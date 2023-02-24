package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.*
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class ArrangorVeilederServiceImpl (
	private val arrangorAnsattService: ArrangorAnsattService,
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

	override fun hentAktiveVeiledereForGjennomforing(gjennomforingId: UUID): List<ArrangorVeileder> {
		val deltakerIder = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId).map { it.id }

		return arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder).map { it.toArrangorVeileder() }
	}

	override fun hentTilgjengeligeVeiledereForGjennomforing(gjennomforingId: UUID): List<Ansatt> {
		val arrangorId = gjennomforingService.getGjennomforing(gjennomforingId).arrangor.id

		// Henter alle veiledere hos arrangør inntil noen form for veileder-gjennomføringtilgang er implementert.
		return arrangorAnsattService.getVeiledereForArrangor(arrangorId)
	}

	override fun erVeilederFor(ansattId: UUID, deltakerId: UUID): Boolean {
		return hentVeiledereForDeltaker(deltakerId).any { it.ansattId == ansattId }
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

	private fun verifiserVeilederTilganger(deltakerIder: List<UUID>, veilederIder: List<UUID>) {
		val gjennomforingIder = deltakerService.hentDeltakere(deltakerIder)
			.map { it.gjennomforingId }.distinct()

		if (gjennomforingIder.size > 1) {
			throw ValidationException("Alle deltakere må være på samme gjennomføring for å tildele veiledere")
		}

		val arrangorId = gjennomforingService.getGjennomforing(gjennomforingIder.first()).arrangor.id

		veilederIder.forEach {
			arrangorAnsattTilgangService.verifiserTilgangTilArrangor(it, arrangorId, ArrangorAnsattRolle.VEILEDER)
		}
	}

}

internal data class OpprettVeilederDbo(
	val ansattId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime = ZonedDateTime.now(),
	val gyldigTil: ZonedDateTime = ZonedDateTime.parse("3000-01-01T00:00Z"),
)
