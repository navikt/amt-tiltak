package no.nav.amt.tiltak.bff.nav_ansatt.dto

import java.util.*

data class HentGjennomforingerDto(
	val id: UUID,
	val navn: String,
	val arrangorNavn: String,
	val lopenr: Int,
	val opprettetAar: Int,
	val antallAktiveEndringsmeldinger: Int,
	val harSkjermedeDeltakere: Boolean,
	val tiltak: TiltakDto,
)
