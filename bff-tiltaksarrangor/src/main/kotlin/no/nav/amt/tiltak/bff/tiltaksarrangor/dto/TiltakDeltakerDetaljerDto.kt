package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltakDeltakerDetaljerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
	val telefonnummer: String?,
	val epost: String?,
	val navEnhet: NavEnhetDto?,
	val navVeileder: NavVeilederDto?,
	val erSkjermetPerson: Boolean,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val status: DeltakerStatusDto,
	val gjennomforing: GjennomforingDto,
	val fjernesDato: LocalDateTime?,
	val innsokBegrunnelse: String?
)
