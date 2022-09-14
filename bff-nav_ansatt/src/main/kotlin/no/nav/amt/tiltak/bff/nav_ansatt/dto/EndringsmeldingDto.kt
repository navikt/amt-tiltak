package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val bruker: BrukerDto,
	val startDato: LocalDate?,
	val aktiv: Boolean,
	val godkjent: Boolean,
	val arkivert: Boolean,
	val opprettetAvArrangorAnsatt: ArrangorAnsattDto,
	val opprettetDato: LocalDateTime,
)

data class BrukerDto (
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
)

fun Bruker.toDto() = BrukerDto(
	fornavn = fornavn,
	mellomnavn = mellomnavn,
	etternavn = etternavn,
	fodselsnummer = fodselsnummer,
)

data class ArrangorAnsattDto (
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String
)

fun Ansatt.toDto() = ArrangorAnsattDto (
	fornavn = fornavn,
	mellomnavn = mellomnavn,
	etternavn = etternavn,
)
