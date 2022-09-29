package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class GjennomforingPayload(
	val id: UUID,
	val tiltak: Tiltak,
	val virksomhetsnummer: String,
	val navn: String,
	val status: Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val fremmoteDato: LocalDateTime?,
	val ansvarligNavEnhetId: String?,
	val opprettetAar: Int,
	val lopenr: Int,
) {
	enum class Status {
		IKKE_STARTET, GJENNOMFORES, AVSLUTTET
	}
}

data class Tiltak(
	val id: UUID,
	val kode: String,
	val navn: String
)
