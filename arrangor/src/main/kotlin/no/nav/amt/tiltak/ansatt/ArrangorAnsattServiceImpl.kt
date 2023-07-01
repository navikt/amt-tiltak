package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class ArrangorAnsattServiceImpl(
	private val arrangorAnsattRepository: ArrangorAnsattRepository,
	private val arrangorService: ArrangorService,
	private val dataPublisherService: DataPublisherService,
) : ArrangorAnsattService {

	private val log = LoggerFactory.getLogger(javaClass)

	// Forhindrer circular dependency, må fikses når vi rydder opp i arkitekturen
	@Lazy
	@Autowired
	lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	override fun upsertAnsatt(arrangorAnsatt: ArrangorAnsatt) {
		val maybeAnsatt = arrangorAnsattRepository.getByPersonligIdent(arrangorAnsatt.personalia.personident)
		if (maybeAnsatt != null && maybeAnsatt.id != arrangorAnsatt.id) {
			throw IllegalStateException("Det finnes allerede en ansatt for samme personident: gammel id: ${maybeAnsatt.id}, ny id: ${arrangorAnsatt.id}")
		}
		arrangorAnsattRepository.upsertAnsatt(
			id = arrangorAnsatt.id,
			personligIdent = arrangorAnsatt.personalia.personident,
			fornavn = arrangorAnsatt.personalia.navn.fornavn,
			mellomnavn = arrangorAnsatt.personalia.navn.mellomnavn,
			etternavn = arrangorAnsatt.personalia.navn.etternavn
		)

		dataPublisherService.publish(arrangorAnsatt.id, DataPublishType.ARRANGOR_ANSATT)
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		val ansattDbo = arrangorAnsattRepository.get(ansattId) ?: throw NoSuchElementException("Ansatt ikke funnet")
		val arrangorer = hentTilknyttedeArrangorer(ansattId)

		return ansattDbo.toAnsatt(arrangorer)
	}

	override fun getAnsatte(ansattIder: List<UUID>): List<Ansatt> {
		return arrangorAnsattRepository.getAnsatte(ansattIder).map { it.toAnsatt(emptyList()) }
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt? {
		arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(personIdent)
		val ansattDbo = arrangorAnsattRepository.getByPersonligIdent(personIdent) ?: return null
		val arrangorer = hentTilknyttedeArrangorer(ansattDbo.id)

		return ansattDbo.toAnsatt(arrangorer)
	}

	override fun getAnsattIdByPersonligIdent(personIdent: String) : UUID {
		return arrangorAnsattRepository.getByPersonligIdent(personIdent)?.id
			?: throw UnauthorizedException("Fant ikke ansatt")
	}

	override fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): List<Ansatt> {
		return arrangorAnsattRepository.getAnsatteForGjennomforing(gjennomforingId, ArrangorAnsattRolle.KOORDINATOR)
			.map { it.toAnsatt(emptyList()) }
	}

	override fun getVeiledereForArrangor(arrangorId: UUID): List<Ansatt> {
		return arrangorAnsattRepository.getAnsatteMedRolleForArrangor(arrangorId, ArrangorAnsattRolle.VEILEDER)
			.map { it.toAnsatt(emptyList()) }
	}

	override fun setTilgangerSistSynkronisert(ansattId: UUID, sistOppdatert: LocalDateTime) {
		arrangorAnsattRepository.setSistOppdatertForAnsatt(ansattId, sistOppdatert)
	}

	override fun getAnsatteSistSynkronisertEldreEnn(eldreEnn: LocalDateTime, maksAntall: Int): List<Ansatt> {
		return arrangorAnsattRepository.getEldsteSistRolleSynkroniserteAnsatte(maksAntall)
			.filter { it.tilgangerSistSynkronisert.isBefore(eldreEnn) }
			.map {
				val arrangorer = hentTilknyttedeArrangorer(it.id)
				it.toAnsatt(arrangorer)
			}
	}

	override fun setVellykketInnlogging(ansattId: UUID) {
		arrangorAnsattRepository.setVelykketInnlogging(ansattId)
	}

	private fun hentTilknyttedeArrangorer(ansattId: UUID): List<TilknyttetArrangor> {
		val roller = arrangorAnsattTilgangService.hentAnsattTilganger(ansattId)
		val arrangorIder = roller.map { it.arrangorId }

		return arrangorService.getArrangorerById(arrangorIder).map {
			val arrangorRoller = roller.find { r -> r.arrangorId == it.id }
				?: throw IllegalStateException("Fant ikke roller")
			return@map TilknyttetArrangor(
				id = it.id,
				navn = it.navn,
				organisasjonsnummer = it.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = it.overordnetEnhetOrganisasjonsnummer,
				overordnetEnhetNavn = it.overordnetEnhetNavn,
				roller = arrangorRoller.roller
			)
		}
	}
}
