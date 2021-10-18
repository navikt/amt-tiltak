package no.nav.amt.tiltak.tiltaksleverandor

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import no.nav.amt.tiltak.core.port.Tiltaksleverandor
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.AnsattService
import org.springframework.stereotype.Service
import java.util.*

@Service
class TiltaksleverandorService(
	private val ansattService: AnsattService // Trenger ikke dette enda
) : Tiltaksleverandor {

	override fun addVirksomhet(virksomhetsnummer: String): Virksomhet {
		TODO("Not yet implemented")
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		return ansattService.getAnsatt(ansattId)
	}

	override fun getVirksomheterForAnsatt(ansattId: UUID): List<Virksomhet> {
		TODO("Not yet implemented")
	}
}
