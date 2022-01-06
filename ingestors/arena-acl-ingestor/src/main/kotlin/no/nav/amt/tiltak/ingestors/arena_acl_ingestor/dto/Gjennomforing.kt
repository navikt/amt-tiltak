package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Gjennomforing(
	val id: UUID,
	val tiltak: Tiltak,
	val virksomhetsnummer: String,
	val navn: String,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrert: LocalDateTime,
	val fremmote: LocalDateTime?
)

data class Tiltak(
	val id: UUID,
	val kode: String,
	val navn: String
)
