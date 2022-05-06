package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
internal class NavAnsattServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
	private val veilederConnector: VeilederConnector,
) : NavAnsattService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		return veilederConnector.hentVeileder(navIdent)
			?: throw NoSuchElementException("Fant ikke nav ansatt med ident $navIdent")
	}

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

	override fun getOrCreateNavAnsatt(navIdent: String): NavAnsatt {
		val veileder = getLagretNavAnsatt(navIdent)

		if (veileder != null)
			return veileder

		val nyVeileder = veilederConnector.hentVeileder(navIdent)

		if (nyVeileder == null) {
			log.error("Klarte ikke å hente nav ansatt med ident $navIdent")
			throw IllegalArgumentException("Klarte ikke å finne nav ansatt med ident")
		}

		log.info("Oppretter ny nav ansatt for nav ident $navIdent")

		upsertVeileder(nyVeileder)

		return getLagretNavAnsatt(navIdent) ?: throw IllegalStateException("Fant ikke nylig opprettet nav ansatt")
	}

	private fun getLagretNavAnsatt(navIdent: String): NavAnsatt? =
		navAnsattRepository.getNavAnsattWithIdent(navIdent)?.toNavAnsatt()

	internal fun getNavAnsattBatch(bucket: Bucket) =
		navAnsattRepository.getNavAnsattInBucket(bucket).map { it.toNavAnsatt() }

	private fun NavAnsattDbo.toNavAnsatt() = NavAnsatt(
		id = id,
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)
}
