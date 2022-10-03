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
	val navEnhet: NavEnhet?,
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

	fun utledStatus(): Status {
		val now = LocalDate.now()

		val sluttDato = sluttDato ?: LocalDate.now().plusYears(1000)

		if(status.type == Status.VENTER_PA_OPPSTART && startDato == null)
			return status.type

		if(status.type == Status.VENTER_PA_OPPSTART && now.isAfter(startDato!!.minusDays(1)) && now.isBefore(sluttDato.plusDays(1)))
			return Status.DELTAR

		if(listOf(Status.DELTAR, Status.VENTER_PA_OPPSTART).contains(status.type) && now.isAfter(sluttDato))
			return Status.HAR_SLUTTET

		return status.type
	}
}

