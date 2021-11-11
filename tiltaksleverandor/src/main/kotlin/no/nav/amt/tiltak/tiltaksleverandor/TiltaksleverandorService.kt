package no.nav.amt.tiltak.tiltaksleverandor

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.AnsattService
import no.nav.amt.tiltak.tiltaksleverandor.repositories.TiltaksleverandorRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TiltaksleverandorService(
	private val ansattService: AnsattService, // Trenger ikke dette enda
	private val enhetsregisterConnector: EnhetsregisterConnector,
	private val tiltaksleverandorRepository: TiltaksleverandorRepository
) : TiltaksleverandorService {

	override fun addTiltaksleverandor(virksomhetsnummer: String): Tiltaksleverandor {
		val tiltaksleverandor = enhetsregisterConnector.hentVirksomhet(virksomhetsnummer)

		return tiltaksleverandorRepository.insert(
			organisasjonsnavn = tiltaksleverandor.overordnetEnhetNavn,
			organisasjonsnummer = tiltaksleverandor.overordnetEnhetOrganisasjonsnummer,
			virksomhetsnavn = tiltaksleverandor.navn,
			virksomhetsnummer = tiltaksleverandor.organisasjonsnummer,
		).toTiltaksleverandor()
	}

	override fun getTiltaksleverandorByVirksomhetsnummer(virksomhetsnummer: String): Tiltaksleverandor? {
		return tiltaksleverandorRepository.getByVirksomhetsnummer(virksomhetsnummer)?.toTiltaksleverandor()
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		return ansattService.getAnsatt(ansattId)
	}

	override fun getVirksomheterForAnsatt(ansattId: UUID): List<Tiltaksleverandor> {
		TODO("Not yet implemented")
	}
}
