package no.nav.amt.tiltak.clients.mulighetsrommet_api_client

import java.time.LocalDate
import java.util.UUID

interface MulighetsrommetApiClient {

	fun hentGjennomforingArenaData(id: UUID): GjennomforingArenaData

	fun hentGjennomforing(id: UUID): Gjennomforing
}

data class GjennomforingArenaData(
	val opprettetAar: Int,
	val lopenr: Int,
	val virksomhetsnummer: String?,
	val ansvarligNavEnhetId: String?,
	val status: String,
)


data class Gjennomforing (
	val id: UUID,
	val tiltakstype: Tiltakstype,
	val navn: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate? = null,
	val status: Status,
	val virksomhetsnummer: String
) {

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

	fun erKurs() : Boolean {
		return listOf(
			"JOBBK",
			"GRUPPEAMO",
			"GRUFAGYRKE"
		).contains(tiltakstype.arenaKode)
	}
}
