package no.nav.amt.tiltak.adapter.input

import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/tiltak")
class TiltakController {

	@GetMapping("/instanser")
	fun hentTiltakInstanser(@RequestParam bedriftsnummer: String): List<TiltakInstansDTO> {
		TODO()
	}

	@GetMapping("/instans/{id}/brukere")
	fun hentDeltagerePaTiltak(@PathVariable id: UUID): List<DeltagerDTO> {
		TODO()
	}

	@GetMapping("/instans/{id}")
	fun hentTiltakInstansDetaljer(@PathVariable id: UUID): TiltakInstansDTO {
		TODO()
	}

}

data class TiltakInstansDTO (
	val id: UUID,
	val navn: String,
	val oppstartsdato: LocalDateTime,
	val sluttdato: LocalDateTime,
	val tiltakskode: String,
	val tiltaksnavn: String
)

data class DeltagerDTO (
	val fornavn: String,
	val etternavn: String,
	val fodselsdato: String,
	val startdato: LocalDateTime,
	val sluttdato: LocalDateTime,
	val status: String // TODO: Convert to enum
)
