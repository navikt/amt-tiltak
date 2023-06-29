package no.nav.amt.tiltak.external.api.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus

enum class DeltakerStatusDto {
	VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, VURDERES, AVBRUTT
}

fun DeltakerStatus.toDto() = DeltakerStatusDto.valueOf(type.toString())
