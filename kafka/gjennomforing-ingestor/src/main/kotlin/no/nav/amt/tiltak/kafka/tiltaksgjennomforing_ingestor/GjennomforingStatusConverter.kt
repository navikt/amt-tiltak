package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing

object GjennomforingStatusConverter {

	private val avsluttendeStatuser = listOf(GjennomforingMessage.Status.AVLYST, GjennomforingMessage.Status.AVSLUTTET, GjennomforingMessage.Status.AVBRUTT).map { it.name }
	private val ikkeStartetStatuser = listOf(GjennomforingMessage.Status.APENT_FOR_INNSOK.name)
	private val gjennomforesStatuser = listOf(GjennomforingMessage.Status.GJENNOMFORES.name)

	fun convert (status: String) : Gjennomforing.Status {
		return when (status) {
			in avsluttendeStatuser -> Gjennomforing.Status.AVSLUTTET
			in ikkeStartetStatuser -> Gjennomforing.Status.IKKE_STARTET
			in gjennomforesStatuser -> Gjennomforing.Status.GJENNOMFORES
			else -> throw RuntimeException("Ukjent status fra mulighetsrommet: $status")
		}

	}
}
