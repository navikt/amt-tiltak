package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak

data class TiltakDto(
	val tiltakskode: String,
	val tiltaksnavn: String,
)

fun Tiltak.toDto() = TiltakDto(
	tiltakskode = this.kode,
	tiltaksnavn = this.navn
)
