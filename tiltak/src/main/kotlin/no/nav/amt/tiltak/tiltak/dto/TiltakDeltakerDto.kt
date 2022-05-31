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
	val status: DeltakerStatusDto,
	val registrertDato: LocalDateTime
)

fun Deltaker.toDto() = TiltakDeltakerDto(
	id = id,
	fornavn = requireNotNull(bruker).fornavn,
	mellomnavn = requireNotNull(bruker).mellomnavn,
	etternavn = requireNotNull(bruker).etternavn,
	fodselsnummer = requireNotNull(bruker).fodselsnummer,
	startDato = startDato,
	sluttDato = sluttDato,
	status = DeltakerStatusDto(statuser.current.status, statuser.current.statusGjelderFra, statuser.current.opprettetDato),
	registrertDato = registrertDato
)
