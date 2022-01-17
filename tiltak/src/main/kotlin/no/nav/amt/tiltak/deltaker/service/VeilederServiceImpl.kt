package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.deltaker.repositories.NavAnsattRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class VeilederServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
) : VeilederService {

	override fun upsertVeileder(veileder: Veileder): UUID {
		navAnsattRepository.upsert(
			UpsertNavAnsattCommand(
				personligIdent = veileder.navIdent,
				navn = veileder.navn,
				epost = veileder.epost,
				telefonnummer = veileder.telefonnummer
			)
		)

		return navAnsattRepository.getNavAnsattWithIdent(veileder.navIdent)?.id
			?: throw IllegalStateException("Fant ikke veileder med NAV-ident=${veileder.navIdent}")
	}

	override fun getVeileder(navIdent: String): Veileder? {
		val veileder = navAnsattRepository.getNavAnsattWithIdent(navIdent) ?: return null

		return Veileder(
			navIdent = veileder.personligIdent,
			navn = veileder.navn,
			epost = veileder.epost,
			telefonnummer = veileder.telefonnummer
		)
	}


}
