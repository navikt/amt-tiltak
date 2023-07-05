package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class ArrangorAnsattServiceImpl(
	private val arrangorAnsattRepository: ArrangorAnsattRepository,
	private val arrangorService: ArrangorService
) : ArrangorAnsattService {

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
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		val ansattDbo = getAnsattFraDbEllerAmtArrangor(ansattId) ?: throw NoSuchElementException("Ansatt ikke funnet")
		val arrangorer = hentTilknyttedeArrangorer(ansattId)

		return ansattDbo.toAnsatt(arrangorer)
	}

	private fun getAnsattFraDbEllerAmtArrangor(ansattId: UUID): AnsattDbo? {
		val maybeAnsattDbo = arrangorAnsattRepository.get(ansattId)
		if (maybeAnsattDbo != null) {
			return maybeAnsattDbo
		} else {
			arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(ansattId)
			return arrangorAnsattRepository.get(ansattId)
		}
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt? {
		arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(personIdent)
		val ansattDbo = arrangorAnsattRepository.getByPersonligIdent(personIdent) ?: return null
		val arrangorer = hentTilknyttedeArrangorer(ansattDbo.id)

		return ansattDbo.toAnsatt(arrangorer)
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
