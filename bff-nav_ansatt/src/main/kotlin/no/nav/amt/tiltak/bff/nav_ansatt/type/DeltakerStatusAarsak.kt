package no.nav.amt.tiltak.bff.tiltaksarrangor.type

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus


data class DeltakerStatusAarsak (
	val type: Type,
	val beskrivelse: String? = null,
) {
	enum class Type {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, UTDANNING, FERDIG, AVLYST_KONTRAKT, IKKE_MOTT, FEILREGISTRERT, ANNET;

		fun toDeltakerStatusAarsak(): Deltaker.StatusAarsak {
			return when(this) {
				SYK -> Deltaker.StatusAarsak.SYK
				FATT_JOBB -> Deltaker.StatusAarsak.FATT_JOBB
				TRENGER_ANNEN_STOTTE -> Deltaker.StatusAarsak.TRENGER_ANNEN_STOTTE
				FIKK_IKKE_PLASS -> Deltaker.StatusAarsak.FIKK_IKKE_PLASS
				UTDANNING -> Deltaker.StatusAarsak.UTDANNING
				FERDIG -> Deltaker.StatusAarsak.FERDIG
				AVLYST_KONTRAKT -> Deltaker.StatusAarsak.AVLYST_KONTRAKT
				IKKE_MOTT -> Deltaker.StatusAarsak.IKKE_MOTT
				FEILREGISTRERT -> Deltaker.StatusAarsak.FEILREGISTRERT
				ANNET -> Deltaker.StatusAarsak.ANNET
			}
		}
	}

	fun toModel(): DeltakerStatus.Aarsak {
		val type = this.type.toDeltakerStatusAarsak()
		return DeltakerStatus.Aarsak(type, this.beskrivelse)
	}


}

fun DeltakerStatus.Aarsak.toDto(): DeltakerStatusAarsak {
	return when(this.type) {
		Deltaker.StatusAarsak.SYK -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.SYK)
		Deltaker.StatusAarsak.FATT_JOBB -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FATT_JOBB)
		Deltaker.StatusAarsak.TRENGER_ANNEN_STOTTE -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.TRENGER_ANNEN_STOTTE)
		Deltaker.StatusAarsak.FIKK_IKKE_PLASS -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FIKK_IKKE_PLASS)
		Deltaker.StatusAarsak.UTDANNING -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.UTDANNING)
		Deltaker.StatusAarsak.FERDIG -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FERDIG)
		Deltaker.StatusAarsak.AVLYST_KONTRAKT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.AVLYST_KONTRAKT)
		Deltaker.StatusAarsak.IKKE_MOTT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.IKKE_MOTT)
		Deltaker.StatusAarsak.FEILREGISTRERT -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.FEILREGISTRERT)
		Deltaker.StatusAarsak.ANNET -> DeltakerStatusAarsak(DeltakerStatusAarsak.Type.ANNET, this.beskrivelse)
	}
}
