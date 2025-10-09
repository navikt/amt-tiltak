package no.nav.amt.tiltak.clients.mulighetsrommet_api_client

import java.time.LocalDate
import java.util.UUID

interface MulighetsrommetApiClient {

	fun hentGjennomforingArenaData(id: UUID): GjennomforingArenaData?

	fun hentGjennomforing(id: UUID): GjennomforingResponse
}

data class GjennomforingArenaData(
	val opprettetAar: Int,
	val lopenr: Int
)


data class GjennomforingResponse (
	val id: UUID,
	val tiltakstype: Tiltakstype,
	val navn: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate? = null,
	val status: Status,
	val virksomhetsnummer: String,
	val oppstart: Oppstartstype
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
		return oppstart == Oppstartstype.FELLES
	}
}
