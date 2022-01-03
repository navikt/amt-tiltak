package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.*

data class DeltakerDetaljerDbo(
	val deltakerId: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val telefonnummer: String?,
	val epost: String?,
	val veilederNavn: String?,
	val veilederTelefonnummer: String?,
	val veilederEpost: String?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Deltaker.Status?,
	val gjennomforingId: UUID,
	val gjennomforingNavn: String,
	val gjennomforingOppstartDato: LocalDate?,
	val gjennomforingSluttDato: LocalDate?,
	val gjennomforingStatus: Gjennomforing.Status?,
	val tiltakNavn: String,
	val tiltakKode: String
)
