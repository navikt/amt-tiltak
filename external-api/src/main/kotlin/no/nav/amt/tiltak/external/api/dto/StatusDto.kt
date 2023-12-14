package no.nav.amt.tiltak.external.api.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus

enum class DeltakerStatusDto {
	UTKAST_TIL_PAMELDING, AVBRUTT_UTKAST,
	VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, FULLFORT, IKKE_AKTUELL, FEILREGISTRERT,
	SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, PABEGYNT_REGISTRERING
}

fun DeltakerStatus.toDto() = DeltakerStatusDto.valueOf(type.toString())
