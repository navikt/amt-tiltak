package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArrangorServiceImpl(
	private val amtArrangorService: AmtArrangorService,
	private val arrangorRepository: ArrangorRepository,
) : ArrangorService {

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

	override fun getOrCreateArrangorByOrgnr(organisasjonsnummer: String): Arrangor {
		val maybeArrangor = arrangorRepository.getByOrganisasjonsnummer(organisasjonsnummer)
		if (maybeArrangor != null) return maybeArrangor.toArrangor()

		val arrangor = amtArrangorService.getArrangor(organisasjonsnummer)
			?: throw RuntimeException("Kunne ikke hente arrangør med orgnummer $organisasjonsnummer")

		return arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
		).toArrangor()
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

	override fun updateArrangor(arrangor: Arrangor): Arrangor {
		return arrangorRepository.upsert(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer
		).toArrangor()
	}

	override fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor? {
		return arrangorRepository.getByOrganisasjonsnummer(virksomhetsnummer)?.toArrangor()
	}
}
