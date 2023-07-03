package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArrangorServiceImpl(
	private val amtArrangorService: AmtArrangorService,
	private val arrangorRepository: ArrangorRepository
) : ArrangorService {

	override fun upsertArrangor(virksomhetsnummer: String): Arrangor {
		val arrangor = amtArrangorService.getArrangor(virksomhetsnummer) ?: throw RuntimeException("Kunne ikke hente arrangør med orgnummer $virksomhetsnummer")

		return arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		).toArrangor()
	}

	override fun upsertArrangor(arrangor: Arrangor) {
		arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		)
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
	}
}
