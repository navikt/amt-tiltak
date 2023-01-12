package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

object KafkaMessageCreator {
	fun opprettAmtTiltakDeltakerMessage(
		operation: String = "CREATED",
		payload: DeltakerMessagePayload,

	): String {
		return """
			{
			  "transactionId": "${UUID.randomUUID()}",
			  "type": "DELTAKER",
			  "timestamp": "${ZonedDateTime.now()}",
			  "operation": $operation,
			  "payload": {
			    "id": "${payload.id}",
			    "gjennomforingId": "${payload.gjennomforingId}",
			    "personIdent": "${payload.personIdent}",
				"startDato": ${nullableStringJsonValue(payload.startDato.toString())},
				"sluttDato": ${nullableStringJsonValue(payload.sluttDato.toString())},
			    "status": "${payload.status.name}",
				"statusAarsak": ${nullableStringJsonValue(payload.statusAarsak?.name)}
			    "dagerPerUke": ${payload.dagerPerUke},
			    "prosentDeltid": ${payload.prosentDeltid},
			    "registrertDato": "${payload.registrertDato}",
				"statusEndretDato": ${nullableStringJsonValue(payload.statusEndretDato.toString())},
				"innsokBegrunnelse": ${nullableStringJsonValue(payload.innsokBegrunnelse)}
			  }
			}
		""".trimIndent()
	}

	fun opprettGjennomforingMessage(payload: GjennomforingMessagePayload): String {
		return """
			{
				"id": "${payload.id}",
				"tiltakstype": {
					"id": "${payload.tiltakId}",
					"navn": "${payload.tiltakNavn}",
					"arenaKode": "${payload.tiltakArenaKode}"
				},
				"navn": "${payload.navn}",
				"startDato": ${nullableStringJsonValue(payload.startDato?.toString())},
				"sluttDato": ${nullableStringJsonValue(payload.sluttDato?.toString())}
			}
		""".trimIndent()

	}

	private fun nullableStringJsonValue(str: String?): String {
		return if (str == null) {
			 "null"
		} else {
			"\"$str\""
		}
	}
}

data class GjennomforingMessagePayload (
	val id: UUID = UUID.randomUUID(),
	val tiltakId: UUID = UUID.randomUUID(),
	val tiltakNavn: String = "Oppfølging",
	val tiltakArenaKode: String = "INDOPPFAG",
	val navn: String = "Gjennomføring",
	val startDato: LocalDate? = LocalDate.now().minusMonths(6),
	val sluttDato: LocalDate? = LocalDate.now().plusMonths(12),
)

data class DeltakerMessagePayload (
	val id: UUID = UUID.randomUUID(),
	val personIdent: String = "1234",
	val startDato: LocalDate? = LocalDate.now().plusWeeks(2),
	val sluttDato: LocalDate? = LocalDate.now().plusMonths(6),
	val gjennomforingId: UUID = UUID.randomUUID(),
	val status: DeltakerStatus.Type = DeltakerStatus.Type.VENTER_PA_OPPSTART,
	val statusAarsak: DeltakerPayload.StatusAarsak? = null,
	val dagerPerUke: Int? = 5,
	val prosentDeltid: Float? = null,
	val registrertDato: LocalDateTime = LocalDateTime.now().minusDays(1),
	val statusEndretDato: LocalDateTime? = LocalDateTime.now().plusDays(1),
	val innsokBegrunnelse: String? = "Begrunnelse",
)
