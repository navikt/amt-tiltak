package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltakDeltakerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Deltaker.Status?,
	val registrertDato: LocalDateTime
)

fun Deltaker.toDto() = TiltakDeltakerDto(
	id = id,
	fornavn = fornavn,
	etternavn = etternavn,
	fodselsnummer = fodselsnummer,
	startDato = startDato,
	sluttDato = sluttDato,
	status = status,
	registrertDato = registrertDato
)
