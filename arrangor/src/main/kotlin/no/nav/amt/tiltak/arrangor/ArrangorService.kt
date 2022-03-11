package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.ansatt.AnsattService
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.port.ArrangorService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArrangorService(
	private val ansattService: AnsattService, // Trenger ikke dette enda
	private val enhetsregisterClient: EnhetsregisterClient,
	private val arrangorRepository: ArrangorRepository
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

	override fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor? {
		return arrangorRepository.getByOrganisasjonsnummer(virksomhetsnummer)?.toArrangor()
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		return ansattService.getAnsatt(ansattId)
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt {
		return ansattService.getAnsattByPersonligIdent(personIdent)
	}

	override fun getVirksomheterForAnsatt(ansattId: UUID): List<Arrangor> {
		throw NotImplementedError("getVirksomheterForAnsatt in ArrangorService is not yet implemented")
	}
}
