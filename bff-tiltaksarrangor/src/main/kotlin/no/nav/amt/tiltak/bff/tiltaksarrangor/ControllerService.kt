package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDetaljerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.NavEnhetDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.VeilederDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.skjulesForAlleAktorer
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.*

@Service
open class ControllerService(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val navAnsattService: NavAnsattService,
	private val authService: AuthService
) {

	open fun getDeltakerDetaljerById(deltakerId: UUID): DeltakerDetaljerDto {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Deltaker med id $deltakerId finnes ikke")
		val navVeileder = deltaker.navVeilederId?.let { navAnsattService.getNavAnsatt(it)}
		val gjennomforing = deltaker.gjennomforingId.let { gjennomforingService.getGjennomforing(it) }

		if (deltaker.status.type.skjulesForAlleAktorer() ||
			deltaker.erUtdatert ||
			(gjennomforing.erKurs && deltaker.status.type == DeltakerStatus.Type.IKKE_AKTUELL)) throw UnauthorizedException("Har ikke tilgang til id $deltakerId")

		return DeltakerDetaljerDto(
			id = deltaker.id,
			fornavn = deltaker.fornavn,
			mellomnavn = deltaker.mellomnavn,
			etternavn = deltaker.etternavn,
			fodselsnummer = deltaker.personIdent,
			telefonnummer = deltaker.telefonnummer,
			epost = deltaker.epost,
			deltakelseProsent = deltaker.prosentStilling?.toInt(),
			dagerPerUke = deltaker.dagerPerUke,
			navEnhet = deltaker.navEnhet?.let { NavEnhetDto(it.navn) },
			navVeileder = navVeileder?.toDto(),
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			registrertDato = deltaker.registrertDato,
			status = deltaker.status.toDto(),
			gjennomforing = gjennomforing.toDto(),
			fjernesDato = deltaker.skalFjernesDato,
			innsokBegrunnelse = deltaker.innsokBegrunnelse,
		)
	}


	fun mapAnsatteTilVeilederDtoer(veiledere: List<ArrangorVeileder>): List<VeilederDto> {
		val ansatte = arrangorAnsattService.getAnsatte(veiledere.map { it.ansattId })

		return veiledere.map { veileder ->
			val ansatt = ansatte.find { veileder.ansattId == it.id } ?:
			throw IllegalStateException("Fant ikke ansatt ${veileder.ansattId} for veileder ${veileder.id}")

			return@map VeilederDto(
				id = veileder.id,
				ansattId = ansatt.id,
				deltakerId = veileder.deltakerId,
				erMedveileder = veileder.erMedveileder,
				fornavn = ansatt.fornavn,
				mellomnavn = ansatt.mellomnavn,
				etternavn = ansatt.etternavn,
			)
		}
	}

	fun hentInnloggetAnsatt(): Ansatt {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		return arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			.also { MDC.put("ansatt-id", it?.id.toString()) }
			?: throw UnauthorizedException("Arrangor ansatt finnes ikke")
	}

}
