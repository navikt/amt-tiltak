package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.util.*

data class TiltakDeltakerDetaljerDto(
    val id: UUID,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val fodselsnummer: String,
    val telefonnummer: String?,
    val epost: String?,
    val navKontor: NavKontorDTO,
    val navVeileder: NavVeilederDTO?,
    val startdato: LocalDate?,
    val sluttdato: LocalDate?,
    val status: Deltaker.Status?,
    val gjennomforing: GjennomforingDto
)

data class NavKontorDTO(
	val navn: String,
	val adresse: String
)

data class NavVeilederDTO(
	val navn: String,
	val telefon: String?,
	val epost: String?,
)
