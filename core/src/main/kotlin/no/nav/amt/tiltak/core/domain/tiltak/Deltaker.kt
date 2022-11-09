package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Deltaker(
	val id: UUID = UUID.randomUUID(),
	val gjennomforingId: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val fodselsnummer: String,
	val navEnhetId: UUID?,
	val navVeilederId: UUID?,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatus,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String? = null
) {
	val skalFjernesDato = if(status.type == Status.HAR_SLUTTET || status.type == Status.IKKE_AKTUELL) status.gyldigFra.plusWeeks(2) else null
	val erUtdatert = skalFjernesDato != null && LocalDateTime.now().isAfter(skalFjernesDato)

	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT, PABEGYNT
	}

	enum class StatusAarsak {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, UTDANNING, FERDIG, AVLYST_KONTRAKT, IKKE_MOTT, FEILREGISTRERT, ANNET
	}
}

