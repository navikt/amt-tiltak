package no.nav.amt.tiltak.tiltak.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.util.*

data class TiltakDeltakerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
	val startdato: LocalDate?,
	val sluttdato: LocalDate?,
	val status: Deltaker.Status?
)

fun Deltaker.toDto() = TiltakDeltakerDto(
	id = id,
	fornavn = fornavn,
	etternavn = etternavn,
	fodselsnummer = fodselsnummer,
	startdato = startdato,
	sluttdato = sluttdato,
	status = status
)
