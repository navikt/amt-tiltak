package no.nav.amt.tiltak.tiltak.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class TiltakDeltagerDetaljerDto(
    val id: UUID,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val fodselsdato: LocalDate?,
    val telefon: String?,
    val epost: String?,
    val navKontor: NavKontorDTO,
    val navVeileder: NavVeilederDTO,
    val startdato: ZonedDateTime,
    val sluttdato: ZonedDateTime,
    val status: Deltaker.Status,
    val tiltakInstans: TiltakInstansDto
)

data class NavKontorDTO(
	val navn: String,
	val adresse: String
)

data class NavVeilederDTO(
	val navn: String,
	val telefon: String,
	val epost: String,
)
