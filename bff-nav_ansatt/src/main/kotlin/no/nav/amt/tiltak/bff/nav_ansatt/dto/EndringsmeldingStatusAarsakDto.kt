package no.nav.amt.tiltak.bff.nav_ansatt.dto

data class EndringsmeldingStatusAarsakDto(
	val type: Type,
	val beskrivelse: String? = null
){
	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, UTDANNING, IKKE_MOTT, OPPFYLLER_IKKE_KRAVENE, ANNET
	}
}

