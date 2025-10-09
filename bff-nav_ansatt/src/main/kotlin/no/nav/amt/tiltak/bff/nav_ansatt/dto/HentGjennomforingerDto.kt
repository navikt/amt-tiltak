package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.*

data class HentGjennomforingerDto(
	val id: UUID,
	val navn: String,
	val arrangorNavn: String,
	val lopenr: Int?,
	val opprettetAar: Int?,
	val antallAktiveEndringsmeldinger: Int,
	val harSkjermedeDeltakere: Boolean,
	val adressebeskyttelser: List<Adressebeskyttelse>,
	val tiltak: TiltakDto,
	val status: Gjennomforing.Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?
)
