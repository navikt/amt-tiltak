package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDetaljerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.springframework.stereotype.Service
import java.util.*

@Service
open class ControllerService(
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val navAnsattService: NavAnsattService,
	private val navEnhetService: NavEnhetService,
) {

	open fun getDeltakerDetaljerById(deltakerId: UUID): DeltakerDetaljerDto {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Deltaker med id $deltakerId finnes ikke")
		val navVeileder = deltaker.navVeilederId?.let { navAnsattService.getNavAnsatt(it)}
		val navEnhet = deltaker.navEnhetId?.let { navEnhetService.getNavEnhet(it) }
		val gjennomforing = deltaker.gjennomforingId.let { gjennomforingService.getGjennomforing(it) }

		if (deltaker.status.type == DeltakerStatus.Type.PABEGYNT || deltaker.status.type == DeltakerStatus.Type.PABEGYNT_REGISTRERING)
			throw UnauthorizedException("Har ikke tilgang til id $deltakerId")

		return DeltakerDetaljerDto(
			id = deltaker.id,
			fornavn = deltaker.fornavn,
			mellomnavn = deltaker.mellomnavn,
			etternavn = deltaker.etternavn,
			fodselsnummer = deltaker.personIdent,
			telefonnummer = deltaker.telefonnummer,
			epost = deltaker.epost,
			deltakelseProsent = deltaker.prosentStilling?.toInt(),
			navEnhet = navEnhet?.toDto(),
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
}
