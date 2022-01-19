package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltakDeltakerDetaljerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
	val telefonnummer: String?,
	val epost: String?,
	val navKontor: NavKontorDto?,
	val navVeileder: NavVeilederDto?,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val status: Deltaker.Status?,
	val gjennomforing: GjennomforingDto
)

data class NavKontorDto(
	val navn: String,
)

data class NavVeilederDto(
	val navn: String,
	val telefon: String?,
	val epost: String?,
)
