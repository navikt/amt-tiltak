package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing

enum class Status {
	IKKE_STARTET, GJENNOMFORES, AVSLUTTET
}

fun Gjennomforing.Status.toDto(): Status {
	return when(this) {
		Gjennomforing.Status.GJENNOMFORES -> Status.GJENNOMFORES
		Gjennomforing.Status.AVSLUTTET -> Status.AVSLUTTET
		Gjennomforing.Status.IKKE_STARTET -> Status.IKKE_STARTET
	}
}
