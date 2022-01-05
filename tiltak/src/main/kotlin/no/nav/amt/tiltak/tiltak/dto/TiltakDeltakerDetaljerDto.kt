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
	val navKontor: NavKontorDTO,
	val navVeileder: NavVeilederDTO?,
	val oppstartdato: LocalDate?,
	val sluttdato: LocalDate?,
	val registrertDato: LocalDateTime,
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
