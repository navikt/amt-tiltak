package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.AktivEndringsmeldingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerStatusDto
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
	val registrertDato: LocalDateTime,
	val aktivEndringsmelding: AktivEndringsmeldingDto?
)


fun Deltaker.toDto(aktivEndringsmeldingDto: AktivEndringsmeldingDto?) = TiltakDeltakerDto(
	id = id,
	fornavn = bruker.fornavn,
	mellomnavn = bruker.mellomnavn,
	etternavn = bruker.etternavn,
	fodselsnummer = bruker.fodselsnummer,
	startDato = startDato,
	sluttDato = sluttDato,
	status = DeltakerStatusDto(type=status.type, endretDato = status.opprettetDato),
	registrertDato = registrertDato,
	aktivEndringsmelding = aktivEndringsmeldingDto
)
