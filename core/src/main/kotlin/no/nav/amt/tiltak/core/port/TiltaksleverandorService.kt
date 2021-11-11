package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import java.util.*

interface TiltaksleverandorService {

	fun addTiltaksleverandor(virksomhetsnummer: String): Tiltaksleverandor

	fun getVirksomheterForAnsatt(ansattId: UUID): List<Tiltaksleverandor>
	fun getAnsatt(ansattId: UUID): Ansatt
	fun getTiltaksleverandorByVirksomhetsnummer(virksomhetsnummer: String): Tiltaksleverandor?
}
