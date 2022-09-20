package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.TiltakDeltakerDetaljerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import org.springframework.stereotype.Service
import java.util.*

@Service
open class ControllerService(
	private val deltakerService: DeltakerService,
	private val skjermetPersonService: SkjermetPersonService,
	private val gjennomforingService: GjennomforingService,
	private val navAnsattService: NavAnsattService,
) {

	open fun getDeltakerDetaljerById(deltakerId: UUID): TiltakDeltakerDetaljerDto {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Deltaker med id $deltakerId finnes ikke")
		val bruker = deltaker.bruker
		val navVeileder = bruker.navVeilederId?.let { navAnsattService.getNavAnsatt(it)}
		val gjennomforing = deltaker.gjennomforingId.let { gjennomforingService.getGjennomforing(it) }
		val erSkjermet = skjermetPersonService.erSkjermet(bruker.fodselsnummer)

		return TiltakDeltakerDetaljerDto(
			id = deltaker.id,
			fornavn = bruker.fornavn,
			mellomnavn = bruker.mellomnavn,
			etternavn = bruker.etternavn,
			fodselsnummer = bruker.fodselsnummer,
			telefonnummer = bruker.telefonnummer,
			epost = bruker.epost,
			navEnhet = bruker.navEnhet?.toDto(),
			navVeileder = navVeileder?.toDto(),
			erSkjermetPerson = erSkjermet,
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
