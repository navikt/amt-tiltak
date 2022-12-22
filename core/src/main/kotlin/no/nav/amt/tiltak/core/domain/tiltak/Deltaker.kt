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
    val personIdent: String,
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
	val skalFjernesDato = if(status.type == DeltakerStatus.Type.HAR_SLUTTET || status.type == DeltakerStatus.Type.IKKE_AKTUELL) status.gyldigFra.plusWeeks(2) else null
	val erUtdatert = skalFjernesDato != null && LocalDateTime.now().isAfter(skalFjernesDato)
}

