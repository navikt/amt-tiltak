package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.clients.nom.NomClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.UpsertNavAnsattInput
import no.nav.amt.tiltak.core.port.NavAnsattService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
internal class NavAnsattServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
	private val nomClient: NomClient,
) : NavAnsattService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun getNavAnsatt(navAnsattId: UUID): NavAnsatt {
		return navAnsattRepository.get(navAnsattId).toNavAnsatt()
	}

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		val navAnsatt = navAnsattRepository.getNavAnsattWithIdent(navIdent)

		if (navAnsatt != null)
			return navAnsatt.toNavAnsatt()

		val nyNavAnsatt = nomClient.hentNavAnsatt(navIdent)

		if (nyNavAnsatt == null) {
			log.error("Klarte ikke å hente nav ansatt med ident $navIdent")
			throw IllegalArgumentException("Klarte ikke å finne nav ansatt med ident")
		}

		log.info("Oppretter ny nav ansatt for nav ident $navIdent")

		val nyAnsattId = UUID.randomUUID()

		navAnsattRepository.upsert(UpsertNavAnsattInput(
			id = nyAnsattId,
			navIdent = nyNavAnsatt.navIdent,
			navn = nyNavAnsatt.navn,
			epost = nyNavAnsatt.epost,
			telefonnummer = nyNavAnsatt.telefonnummer,
		))

		return navAnsattRepository.get(nyAnsattId).toNavAnsatt()
	}

	override fun upsertNavAnsatt(input: UpsertNavAnsattInput) {
		navAnsattRepository.upsert(input)
	}

	private fun NavAnsattDbo.toNavAnsatt() = NavAnsatt(
		id = id,
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)

}
