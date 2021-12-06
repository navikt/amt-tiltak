package no.nav.amt.tiltak.tiltaksarrangor

import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Tiltaksarrangor
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import no.nav.amt.tiltak.core.port.TiltaksarrangorService
import no.nav.amt.tiltak.ansatt.AnsattService
import org.springframework.stereotype.Service
import java.util.*

@Service
class TiltaksarrangorService(
	private val ansattService: AnsattService, // Trenger ikke dette enda
	private val enhetsregisterConnector: EnhetsregisterConnector,
	private val tiltaksarrangorRepository: TiltaksarrangorRepository
) : TiltaksarrangorService {

	override fun addTiltaksarrangor(virksomhetsnummer: String): Tiltaksarrangor {
		val tiltaksarrangor = enhetsregisterConnector.hentVirksomhet(virksomhetsnummer)

		return tiltaksarrangorRepository.insert(
			navn = tiltaksarrangor.navn,
			organisasjonsnummer = tiltaksarrangor.organisasjonsnummer,
			overordnetEnhetNavn = tiltaksarrangor.overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = tiltaksarrangor.overordnetEnhetOrganisasjonsnummer,
		).toTiltaksarrangor()
	}

	override fun getTiltaksarrangorByVirksomhetsnummer(virksomhetsnummer: String): Tiltaksarrangor? {
		return tiltaksarrangorRepository.getByOrganisasjonsnummer(virksomhetsnummer)?.toTiltaksarrangor()
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		return ansattService.getAnsatt(ansattId)
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt {
		return ansattService.getAnsattByPersonligIdent(personIdent)
	}

	override fun getVirksomheterForAnsatt(ansattId: UUID): List<Tiltaksarrangor> {
		throw NotImplementedError("getVirksomheterForAnsatt in TiltaksarrangorService is not yet implemented")
	}
}
