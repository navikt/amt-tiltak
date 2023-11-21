package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal class NavAnsattServiceImpl(
	private val navAnsattRepository: NavAnsattRepository,
	private val amtPersonClient: AmtPersonClient,
	private val publisherService: DataPublisherService,
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

		return navAnsattRepository.get(nyNavAnsatt.id).toNavAnsatt()
	}

	override fun upsert(ansatt: NavAnsatt) {
		val lagretNavAnsatt = navAnsattRepository.getMaybeNavAnsatt(ansatt.id)?.toNavAnsatt()
		if (lagretNavAnsatt != ansatt) {
			navAnsattRepository.upsert(ansatt)
			navAnsattRepository.getDeltakerIderForNavAnsatt(ansatt.id).forEach {
				publisherService.publish(it, DataPublishType.DELTAKER)
			}
		}
	}

	override fun opprettNavAnsattHvisIkkeFinnes(navAnsattId: UUID) {
		if (navAnsattRepository.getMaybeNavAnsatt(navAnsattId) != null) return

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
