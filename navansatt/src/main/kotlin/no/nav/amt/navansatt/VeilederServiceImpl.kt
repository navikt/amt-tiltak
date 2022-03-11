package no.nav.amt.navansatt

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.VeilederService
import org.springframework.stereotype.Service
import java.util.*

@Service
internal class VeilederServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
) : VeilederService {

	override fun upsertVeileder(veileder: Veileder): UUID {
		navAnsattRepository.upsert(
			NavAnsattDbo(
				navIdent = veileder.navIdent,
				navn = veileder.navn,
				epost = veileder.epost,
				telefonnummer = veileder.telefonnummer
			)
		)

		return navAnsattRepository.getNavAnsattWithIdent(veileder.navIdent)?.id
			?: throw IllegalStateException("Fant ikke veileder med NAV-ident=${veileder.navIdent}")
	}

	override fun getVeileder(navIdent: String) =
		navAnsattRepository.getNavAnsattWithIdent(navIdent)?.let { it.toVeileder() }

	internal fun getVeilederBatch(bucket: NavAnsattBucket) =
		navAnsattRepository.getNavAnsattInBatch(bucket).map { it.toVeileder() }

	private fun NavAnsattDbo.toVeileder() = Veileder(
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)
}
