package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArrangorServiceImpl(
	private val amtArrangorService: AmtArrangorService,
	private val arrangorRepository: ArrangorRepository,
	private val dataPublisherService: DataPublisherService
) : ArrangorService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertArrangor(virksomhetsnummer: String): Arrangor {
		val arrangor = amtArrangorService.getArrangor(virksomhetsnummer) ?: throw RuntimeException("Kunne ikke hente arrangør med orgnummer $virksomhetsnummer")

		return arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		).toArrangor()
			.also { dataPublisherService.publish(it.id, DataPublishType.ARRANGOR) }
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

	override fun getOrCreateArrangor(arrangor: Arrangor): Arrangor {
		val maybeArrangor = arrangorRepository.getByOrganisasjonsnummer(arrangor.organisasjonsnummer)
		if (maybeArrangor != null) return maybeArrangor.toArrangor()

		return arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		).toArrangor()
			.also { dataPublisherService.publish(it.id, DataPublishType.ARRANGOR) }
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
			.also { dataPublisherService.publish(arrangor.id, DataPublishType.ARRANGOR) }

		log.info("Oppdaterte arrangør ${arrangor.id}")
	}

	private fun hentOverordnetEnhet(arrangorUpdate: ArrangorUpdate, original: Arrangor) : OverordnetEnhet {
		if (arrangorUpdate.overordnetEnhetOrganisasjonsnummer != original.overordnetEnhetOrganisasjonsnummer) {
			val nyOverordnetEnhet = arrangorUpdate.overordnetEnhetOrganisasjonsnummer?.let {
				amtArrangorService.getArrangor(it) ?: throw RuntimeException("Kunne ikke hente arrangør med orgnummer $it")
			}
			return OverordnetEnhet(nyOverordnetEnhet?.navn, nyOverordnetEnhet?.organisasjonsnummer)
		}
		return OverordnetEnhet(original.overordnetEnhetNavn, original.overordnetEnhetOrganisasjonsnummer)
	}

	private data class OverordnetEnhet(
		val navn: String?,
		val organisasjonsnummer: String?,
	)

}
