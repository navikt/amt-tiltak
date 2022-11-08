package no.nav.amt.tiltak.bff.tiltaksarrangor.type

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker

enum class DeltakerStatusAarsak {
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

fun Deltaker.StatusAarsak.toDto(): DeltakerStatusAarsak {
	return when(this) {
		Deltaker.StatusAarsak.SYK -> DeltakerStatusAarsak.SYK
		Deltaker.StatusAarsak.FATT_JOBB -> DeltakerStatusAarsak.FATT_JOBB
		Deltaker.StatusAarsak.TRENGER_ANNEN_STOTTE -> DeltakerStatusAarsak.TRENGER_ANNEN_STOTTE
		Deltaker.StatusAarsak.FIKK_IKKE_PLASS -> DeltakerStatusAarsak.FIKK_IKKE_PLASS
		Deltaker.StatusAarsak.UTDANNING -> DeltakerStatusAarsak.UTDANNING
		Deltaker.StatusAarsak.FERDIG -> DeltakerStatusAarsak.FERDIG
		Deltaker.StatusAarsak.AVLYST_KONTRAKT -> DeltakerStatusAarsak.AVLYST_KONTRAKT
		Deltaker.StatusAarsak.IKKE_MOTT -> DeltakerStatusAarsak.IKKE_MOTT
		Deltaker.StatusAarsak.FEILREGISTRERT -> DeltakerStatusAarsak.FEILREGISTRERT
		Deltaker.StatusAarsak.ANNET -> DeltakerStatusAarsak.ANNET
	}
}
