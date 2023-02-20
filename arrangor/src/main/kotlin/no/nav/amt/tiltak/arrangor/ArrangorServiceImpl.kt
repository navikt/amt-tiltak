package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import no.nav.amt.tiltak.core.port.ArrangorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArrangorServiceImpl(
	private val enhetsregisterClient: EnhetsregisterClient,
	private val arrangorRepository: ArrangorRepository,
) : ArrangorService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertArrangor(virksomhetsnummer: String): Arrangor {
		val arrangor = enhetsregisterClient.hentVirksomhet(virksomhetsnummer)

		return arrangorRepository.upsert(
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		).toArrangor()
	}

	override fun getArrangorById(id: UUID): Arrangor {
		return arrangorRepository.getById(id).toArrangor()
	}

	override fun getArrangorerById(arrangorIder: List<UUID>): List<Arrangor> {
		val arrangorer = arrangorRepository.getByIder(arrangorIder).map { it.toArrangor() }
		if (arrangorer.size != arrangorIder.size) {
			throw IllegalStateException("Feil antall arrangorer fra database. arrangorIder.size = ${arrangorIder.size}, arrangorer.size = ${arrangorer.size}")
		}
		return arrangorer
	}

	override fun getOrCreateArrangor(virksomhetsnummer: String): Arrangor {
		val maybeArrangor = arrangorRepository.getByOrganisasjonsnummer(virksomhetsnummer)
		if (maybeArrangor != null) return maybeArrangor.toArrangor()

		return opprettArrangor(virksomhetsnummer)
	}

	override fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor? {
		return arrangorRepository.getByOrganisasjonsnummer(virksomhetsnummer)?.toArrangor()
	}

	override fun oppdaterArrangor(arrangorUpdate: ArrangorUpdate) {
		val arrangor = getArrangorByVirksomhetsnummer(arrangorUpdate.organisasjonsnummer)

		val antallUnderenheter = arrangorRepository.updateUnderenheterIfAny(
			organisasjonsnummer = arrangorUpdate.organisasjonsnummer,
			navn = arrangorUpdate.navn
		)

		if (antallUnderenheter > 0) {
			log.info("Oppdaterte $antallUnderenheter underenheter for virksomhet med orgnr ${arrangorUpdate.organisasjonsnummer}")
		}

		if (arrangor == null) return

		val overordnetEnhet = hentOverordnetEnhet(arrangorUpdate, arrangor)

		arrangorRepository.update(
			ArrangorUpdateDbo(
				id = arrangor.id,
				navn = arrangorUpdate.navn,
				overordnetEnhetNavn = overordnetEnhet.navn,
				overordnetEnhetOrganisasjonsnummer = overordnetEnhet.organisasjonsnummer,
			)
		)

		log.info("Oppdaterte arrangør ${arrangor.id}")
	}

	private fun hentOverordnetEnhet(arrangorUpdate: ArrangorUpdate, original: Arrangor) : OverordnetEnhet {
		if (arrangorUpdate.overordnetEnhetOrganisasjonsnummer != original.overordnetEnhetOrganisasjonsnummer) {
			val nyOverordnetEnhet = arrangorUpdate.overordnetEnhetOrganisasjonsnummer?.let {
				enhetsregisterClient.hentVirksomhet(it)
			}
			return OverordnetEnhet(nyOverordnetEnhet?.navn, nyOverordnetEnhet?.organisasjonsnummer)
		}
		return OverordnetEnhet(original.overordnetEnhetNavn, original.overordnetEnhetOrganisasjonsnummer)
	}

	private fun opprettArrangor(virksomhetsnummer: String): Arrangor {
		val arrangor = enhetsregisterClient.hentVirksomhet(virksomhetsnummer)
		val id = UUID.randomUUID()
		arrangorRepository.insert(
			id = id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		)

		return arrangorRepository.getById(id).toArrangor()
	}

	private data class OverordnetEnhet(
		val navn: String?,
		val organisasjonsnummer: String?,
	)

}
