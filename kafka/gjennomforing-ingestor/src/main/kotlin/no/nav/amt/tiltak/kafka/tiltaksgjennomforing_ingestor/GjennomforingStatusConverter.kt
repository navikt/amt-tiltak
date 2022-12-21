package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing

object GjennomforingStatusConverter {

	private val avsluttendeStatuser = listOf("AVLYST", "AVBRUTT", "AVSLUTT")
	private val ikkeStartetStatuser = listOf("PLANLAGT")
	private val gjennomforesStatuser = listOf("GJENNOMFOR")

	fun convert (arenaStatus: String) : Gjennomforing.Status {
		return when (arenaStatus) {
			in avsluttendeStatuser -> Gjennomforing.Status.AVSLUTTET
			in ikkeStartetStatuser -> Gjennomforing.Status.IKKE_STARTET
			in gjennomforesStatuser -> Gjennomforing.Status.GJENNOMFORES
			else -> throw RuntimeException("Ukjent status fra arena: $arenaStatus")
		}

	}
}
