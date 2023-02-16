package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import no.nav.amt.tiltak.core.port.ArrangorService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Service
class ArrangorServiceImpl(
	private val enhetsregisterClient: EnhetsregisterClient,
	private val arrangorRepository: ArrangorRepository,
	private val transactionTemplate: TransactionTemplate,
) : ArrangorService {

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

		arrangorRepository.updateUnderenheterIfAny(organisasjonsnummer = arrangorUpdate.organisasjonsnummer, navn = arrangorUpdate.navn)

		if (arrangor == null) return

		if (arrangorUpdate.overordnetEnhetOrganisasjonsnummer != arrangor.overordnetEnhetOrganisasjonsnummer) {
			val nyOverordnetEnhet = arrangorUpdate.overordnetEnhetOrganisasjonsnummer?.let {
				enhetsregisterClient.hentVirksomhet(it)
			}
			arrangorRepository.update(
				ArrangorUpdateDbo(
					id = arrangor.id,
					navn = arrangorUpdate.navn,
					overordnetEnhetNavn = nyOverordnetEnhet?.navn,
					overordnetEnhetOrganisasjonsnummer = nyOverordnetEnhet?.organisasjonsnummer,
				)
			)
		}
		else {
			arrangorRepository.update(
				ArrangorUpdateDbo(
					id = arrangor.id,
					navn = arrangorUpdate.navn,
					overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
					overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
				)
			)
		}

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

}
