package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak

data class EndringsmeldingStatusAarsakDto(
	val type: Type,
	val beskrivelse: String? = null
){

	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, UTDANNING, IKKE_MOTT, OPPFYLLER_IKKE_KRAVENE, ANNET
	}


	fun toModel() = EndringsmeldingStatusAarsak(
		type = type.toModel(),
		beskrivelse = beskrivelse
	)

	private fun Type.toModel() = EndringsmeldingStatusAarsak.Type.valueOf(this.name)
}

