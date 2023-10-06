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
		AVSLUTTET,
		APENT_FOR_INNSOK;
	}

	fun erKurs(): Boolean {
		if (oppstart != null) {
			return oppstart == Oppstartstype.FELLES
		} else {
			return kursTiltak.contains(tiltakstype.arenaKode)
		}
	}

	private val kursTiltak = setOf(
		"JOBBK",
		"GRUPPEAMO",
		"GRUFAGYRKE"
	)
}
