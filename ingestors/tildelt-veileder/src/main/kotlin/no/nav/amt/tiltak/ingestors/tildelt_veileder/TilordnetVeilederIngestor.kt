package no.nav.amt.tiltak.ingestors.tildelt_veileder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.core.port.PersonService
import org.springframework.stereotype.Component

@Component
class TilordnetVeilederIngestor(
	private val personService: PersonService
) {

	fun ingest(sisteTilordnetVeilederV1Value: String) {
		val sisteTilordnetVeilederV1 = jacksonObjectMapper().readValue(sisteTilordnetVeilederV1Value, SisteTilordnetVeilederV1::class.java)

		val hentFnr = personService.hentFnr(sisteTilordnetVeilederV1.aktorId)




	}

}
