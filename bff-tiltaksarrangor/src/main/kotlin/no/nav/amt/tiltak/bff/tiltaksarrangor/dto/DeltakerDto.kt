package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val registrertDato: LocalDateTime,
	val aktiveEndringsmeldinger: List<EndringsmeldingDto>
)


fun Deltaker.toDto(aktiveEndringsmeldinger: List<EndringsmeldingDto>) = DeltakerDto(
	id = id,
	fornavn = fornavn,
	mellomnavn = mellomnavn,
	etternavn = etternavn,
	fodselsnummer = personIdent,
	startDato = startDato,
	sluttDato = sluttDato,
	status = DeltakerStatusDto(type=status.type, endretDato = status.opprettetDato),
	registrertDato = registrertDato,
	aktiveEndringsmeldinger = aktiveEndringsmeldinger,
)
