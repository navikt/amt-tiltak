package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.time.LocalDateTime
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
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	var status: Deltaker.Status,
	val statusOpprettet: LocalDateTime,
	val statusGyldigFra: LocalDateTime,
	val navEnhetNavn: String?,
	val gjennomforingId: UUID,
	val gjennomforingNavn: String,
	val gjennomforingStartDato: LocalDate?,
	val gjennomforingSluttDato: LocalDate?,
	val gjennomforingStatus: Gjennomforing.Status?,
	val tiltakNavn: String,
	val tiltakKode: String,
	val virksomhetNavn: String,
	val organisasjonNavn: String?,
)
