package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.VeilederConnector
import no.nav.amt.tiltak.core.port.VeilederService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
internal class VeilederServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
	private val veilederConnector: VeilederConnector,
) : VeilederService {

	private val log = LoggerFactory.getLogger(javaClass)

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

	override fun getOrCreateVeileder(navIdent: String): Veileder {
		val veileder = getVeileder(navIdent)

		if (veileder != null)
			return veileder

		val nyVeileder = veilederConnector.hentVeileder(navIdent)

		if (nyVeileder == null) {
			log.error("Klarte ikke å hente nav ansatt med ident $navIdent")
			throw IllegalArgumentException("Klarte ikke å finne nav ansatt med ident")
		}

		log.info("Oppretter ny nav ansatt for nav ident $navIdent")

		upsertVeileder(nyVeileder)

		return getVeileder(navIdent) ?: throw IllegalStateException("Fant ikke nylig opprettet nav ansatt")
	}

	private fun getVeileder(navIdent: String): Veileder? =
		navAnsattRepository.getNavAnsattWithIdent(navIdent)?.toVeileder()


	internal fun getVeilederBatch(bucket: Bucket) =
		navAnsattRepository.getNavAnsattInBucket(bucket).map { it.toVeileder() }

	private fun NavAnsattDbo.toVeileder() = Veileder(
		id = id,
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)
}
