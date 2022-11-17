package no.nav.amt.tiltak.bff.tiltaksarrangor.type

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus

data class DeltakerStatusAarsak (
	val type: Type,
	val beskrivelse: String? = null,
) {
	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, UTDANNING, FERDIG, AVLYST_KONTRAKT, IKKE_MOTT, FEILREGISTRERT, ANNET;

		fun toDeltakerStatusAarsakType(): DeltakerStatus.Aarsak.Type {
			return when(this) {
				SYK -> DeltakerStatus.Aarsak.Type.SYK
				FATT_JOBB -> DeltakerStatus.Aarsak.Type.FATT_JOBB
				TRENGER_ANNEN_STOTTE -> DeltakerStatus.Aarsak.Type.TRENGER_ANNEN_STOTTE
				FIKK_IKKE_PLASS -> DeltakerStatus.Aarsak.Type.FIKK_IKKE_PLASS
				UTDANNING -> DeltakerStatus.Aarsak.Type.UTDANNING
				FERDIG -> DeltakerStatus.Aarsak.Type.FERDIG
				AVLYST_KONTRAKT -> DeltakerStatus.Aarsak.Type.AVLYST_KONTRAKT
				IKKE_MOTT -> DeltakerStatus.Aarsak.Type.IKKE_MOTT
				FEILREGISTRERT -> DeltakerStatus.Aarsak.Type.FEILREGISTRERT
				ANNET -> DeltakerStatus.Aarsak.Type.ANNET
			}
		}
	}

	fun toModel(): DeltakerStatus.Aarsak {
		val type = this.type.toDeltakerStatusAarsakType()
		return DeltakerStatus.Aarsak(type, this.beskrivelse)
	}


}

fun DeltakerStatus.Aarsak.toDto(): DeltakerStatusAarsak {
	return when(this.type) {
		DeltakerStatus.Aarsak.Type.SYK -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.SYK)
		DeltakerStatus.Aarsak.Type.FATT_JOBB -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FATT_JOBB)
		DeltakerStatus.Aarsak.Type.TRENGER_ANNEN_STOTTE -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.TRENGER_ANNEN_STOTTE)
		DeltakerStatus.Aarsak.Type.FIKK_IKKE_PLASS -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FIKK_IKKE_PLASS)
		DeltakerStatus.Aarsak.Type.UTDANNING -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.UTDANNING)
		DeltakerStatus.Aarsak.Type.FERDIG -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FERDIG)
		DeltakerStatus.Aarsak.Type.AVLYST_KONTRAKT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.AVLYST_KONTRAKT)
		DeltakerStatus.Aarsak.Type.IKKE_MOTT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.IKKE_MOTT)
		DeltakerStatus.Aarsak.Type.FEILREGISTRERT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FEILREGISTRERT)
		DeltakerStatus.Aarsak.Type.ANNET -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.ANNET, this.beskrivelse)
	}
}
