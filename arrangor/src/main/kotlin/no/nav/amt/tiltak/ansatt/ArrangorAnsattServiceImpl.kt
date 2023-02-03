package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.PersonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ArrangorAnsattServiceImpl(
	private val arrangorAnsattRepository: ArrangorAnsattRepository,
	private val personService: PersonService,
	private val arrangorService: ArrangorService,
) : ArrangorAnsattService {

	// Forhindrer circular dependency, må fikses når vi rydder opp i arkitekturen
	@Lazy
	@Autowired
	lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	override fun opprettAnsattHvisIkkeFinnes(personIdent: String): Ansatt {
		return getAnsattByPersonligIdent(personIdent)
			?: createAnsatt(personIdent)
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		val ansattDbo = arrangorAnsattRepository.get(ansattId) ?: throw NoSuchElementException("Ansatt ikke funnet")
		val arrangorer = hentTilknyttedeArrangorer(ansattId)

		return ansattDbo.toAnsatt(arrangorer)
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt? {
		val ansattDbo = arrangorAnsattRepository.getByPersonligIdent(personIdent) ?: return null
		val arrangorer = hentTilknyttedeArrangorer(ansattDbo.id)

		return ansattDbo.toAnsatt(arrangorer)
	}

	override fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): List<Ansatt> {
		return arrangorAnsattRepository.getAnsatteForGjennomforing(gjennomforingId, ArrangorAnsattRolle.KOORDINATOR)
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

	private fun createAnsatt(ansattPersonIdent: String): Ansatt {
		val person = personService.hentPerson(ansattPersonIdent)

		val nyAnsattId = UUID.randomUUID()

		arrangorAnsattRepository.opprettAnsatt(
			id = nyAnsattId,
			personligIdent = ansattPersonIdent,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn
		)

		return getAnsatt(nyAnsattId)
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
				roller = arrangorRoller.roller.map { it.name }
			)
		}
	}
}
