package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import java.time.LocalDate
import java.util.UUID

data class GjennomforingMessage(
	val id: UUID,
	val tiltakstype: Tiltakstype,
	val navn: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate? = null,
	val status: Status,
	val virksomhetsnummer: String,
	val oppstart: Oppstartstype?
) {
	enum class Oppstartstype {
		LOPENDE,
		FELLES
	}

	data class Tiltakstype(
		val id: UUID,
		val navn: String,
		val arenaKode: String
	)

	enum class Status {
		GJENNOMFORES,
		AVBRUTT,
		AVLYST,
		AVSLUTTET;
	}

	fun erKurs(): Boolean {
		return if (oppstart != null) {
			oppstart == Oppstartstype.FELLES
		} else {
			kursTiltak.contains(tiltakstype.arenaKode)
		}
	}

	private val kursTiltak = setOf(
		"JOBBK",
		"GRUPPEAMO",
		"GRUFAGYRKE"
	)
}
