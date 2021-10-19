package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import java.util.*

interface TiltaksleverandorService {

	fun addVirksomhet(virksomhetsnummer: String): Virksomhet

	fun getVirksomheterForAnsatt(ansattId: UUID): List<Virksomhet>
	fun getAnsatt(ansattId: UUID): Ansatt
}
