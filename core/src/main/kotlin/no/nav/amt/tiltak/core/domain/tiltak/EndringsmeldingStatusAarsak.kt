package no.nav.amt.tiltak.core.domain.tiltak

data class EndringsmeldingStatusAarsak(
	val type: Type,
	val beskrivelse: String? = null
) {
	init {
		if (beskrivelse != null && type != Type.ANNET) {
			throw IllegalStateException("Aarsak $type skal ikke ha beskrivelse")
		}
	}
	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, UTDANNING, IKKE_MOTT, ANNET
	}
}

