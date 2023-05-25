package no.nav.amt.tiltak.core.domain.tiltak

data class EndringsmeldingStatusAarsak(
	val type: Type,
	val beskrivelse: String? = null
) {
	init {
		if (beskrivelse != null && type != Type.ANNET && type != Type.OPPFYLLER_IKKE_KRAVENE) {
			throw IllegalStateException("Aarsak $type skal ikke ha beskrivelse")
		}
	}
	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, UTDANNING, IKKE_MOTT, OPPFYLLER_IKKE_KRAVENE, ANNET
	}
}

