package no.nav.amt.tiltak.external.api.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DeltakerDto (
	val id: UUID,
	val gjennomforing: GjennomforingDto,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val dagerPerUke: Float?,
	val prosentStilling: Float?,
	val registrertDato: LocalDateTime,
) {
	private val aktiveStatuser = listOf(
		DeltakerStatusDto.UTKAST_TIL_PAMELDING,
		DeltakerStatusDto.VENTER_PA_OPPSTART,
		DeltakerStatusDto.DELTAR,
		DeltakerStatusDto.SOKT_INN,
		DeltakerStatusDto.VURDERES,
		DeltakerStatusDto.VENTELISTE
	)

	fun erAktiv(): Boolean {
		return status in aktiveStatuser
	}
}
