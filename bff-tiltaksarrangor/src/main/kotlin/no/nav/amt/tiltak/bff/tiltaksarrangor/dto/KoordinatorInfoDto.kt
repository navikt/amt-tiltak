package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.UUID

data class KoordinatorInfoDto(
	val deltakerlister: List<DeltakerlisteDto>
) {
	data class DeltakerlisteDto(
		val id: UUID,
		val navn: String,
		val type: String,
		val startdato: LocalDate?,
		val sluttdato: LocalDate?,
		val erKurs: Boolean
	)
}

fun Gjennomforing.toKoordinatorInfoDeltakerlisteDto() = KoordinatorInfoDto.DeltakerlisteDto(
	id = id,
	navn = navn,
	type = tiltak.navn,
	startdato = startDato,
	sluttdato = sluttDato,
	erKurs = erKurs
)
