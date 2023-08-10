package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal class NavAnsattServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
	private val amtPersonClient: AmtPersonClient,
) : NavAnsattService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun getNavAnsatt(navAnsattId: UUID): NavAnsatt {
		return navAnsattRepository.get(navAnsattId).toNavAnsatt()
	}

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		val navAnsatt = navAnsattRepository.get(navIdent)

		if (navAnsatt != null)
			return navAnsatt.toNavAnsatt()

		val nyNavAnsatt = amtPersonClient.hentNavAnsatt(navIdent).getOrElse {
			log.error("Klarte ikke å hente nav ansatt med ident $navIdent")
			throw it
		}

		log.info("Oppretter ny nav ansatt for nav ident $navIdent")
		upsert(nyNavAnsatt)

		val ansatt = navAnsattRepository.get(nyNavAnsatt.id).toNavAnsatt()

		return ansatt
	}

	override fun upsert(ansatt: NavAnsatt) {
		navAnsattRepository.upsert(ansatt)
	}

	override fun opprettNavAnsattHvisIkkeFinnes(navAnsattId: UUID) {
		if (navAnsattRepository.finnesAnsatt(navAnsattId)) return

		val nyNavAnsatt = amtPersonClient.hentNavAnsatt(navAnsattId).getOrThrow()

		upsert(nyNavAnsatt)
	}

	private fun NavAnsattDbo.toNavAnsatt() = NavAnsatt(
		id = id,
		navIdent = navIdent,
		navn = navn,
		epost = epost,
		telefonnummer = telefonnummer
	)

}
