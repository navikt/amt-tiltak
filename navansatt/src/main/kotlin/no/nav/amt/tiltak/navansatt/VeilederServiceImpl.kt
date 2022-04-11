package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.VeilederService
import org.springframework.stereotype.Component
import java.util.*

@Component
internal class VeilederServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
) : VeilederService {

	override fun upsertVeileder(navAnsatt: NavAnsatt): UUID {
		navAnsattRepository.upsert(
			NavAnsattDbo(
				navIdent = navAnsatt.navIdent,
				navn = navAnsatt.navn,
				epost = navAnsatt.epost,
				telefonnummer = navAnsatt.telefonnummer
			)
		)

		return navAnsattRepository.getNavAnsattWithIdent(navAnsatt.navIdent)?.id
			?: throw IllegalStateException("Fant ikke veileder med NAV-ident=${navAnsatt.navIdent}")
	}

	override fun getVeileder(navIdent: String) =
		navAnsattRepository.getNavAnsattWithIdent(navIdent)?.let { it.toVeileder() }

	internal fun getVeilederBatch(bucket: Bucket) =
		navAnsattRepository.getNavAnsattInBucket(bucket).map { it.toVeileder() }

	private fun NavAnsattDbo.toVeileder() = NavAnsatt(
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)
}
