package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object KafkaMessageCreator {
	fun opprettAmtTiltakDeltakerMessage(msg: DeltakerMessage): String {
		return """
			{
			  "transactionId": "${UUID.randomUUID()}",
			  "type": "DELTAKER",
			  "timestamp": "${LocalDateTime.now()}",
			  "operation": "${msg.operation}",
			  "payload": {
			    "id": "${msg.id}",
			    "gjennomforingId": "${msg.gjennomforingId}",
			    "personIdent": "${msg.personIdent}",
				"startDato": ${nullableStringJsonValue(msg.startDato.toString())},
				"sluttDato": ${nullableStringJsonValue(msg.sluttDato.toString())},
			    "status": "${msg.status.name}",
				"statusAarsak": ${nullableStringJsonValue(msg.statusAarsak?.name)},
			    "dagerPerUke": ${msg.dagerPerUke},
			    "prosentDeltid": ${msg.prosentDeltid},
			    "registrertDato": "${msg.registrertDato}",
				"statusEndretDato": ${nullableStringJsonValue(msg.statusEndretDato.toString())},
				"innsokBegrunnelse": ${nullableStringJsonValue(msg.innsokBegrunnelse)}
			  }
			}
		""".trimIndent()
	}

	fun opprettGjennomforingMessage(msg: GjennomforingMessage): String {
		return """
			{
				"id": "${msg.id}",
				"tiltakstype": {
					"id": "${msg.tiltakId}",
					"navn": "${msg.tiltakNavn}",
					"arenaKode": "${msg.tiltakArenaKode}"
				},
				"virksomhetsnummer":"${msg.virksomhetsnummer}",
				"status": "${msg.status}",
				"navn": "${msg.navn}",
				"startDato": ${nullableStringJsonValue(msg.startDato?.toString())},
				"sluttDato": ${nullableStringJsonValue(msg.sluttDato?.toString())}
			}
		""".trimIndent()

	}

	fun opprettVirksomhetMessage(msg: VirksomhetMessage): String {
		return """
			{
				"organisasjonsnummer": "${msg.organisasjonsnummer}",
				"navn": "${msg.navn}",
				"overordnetEnhetOrganisasjonsnummer": ${nullableStringJsonValue(msg.overordnetEnhetOrganisasjonsnummer)}
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

data class GjennomforingMessage (
	val id: UUID = UUID.randomUUID(),
	val tiltakId: UUID = UUID.randomUUID(),
	val tiltakNavn: String = "Oppfølging",
	val tiltakArenaKode: String = "INDOPPFAG",
	val navn: String = "Gjennomføring",
	val virksomhetsnummer: String = "123",
	val status: String = "GJENNOMFORES",
	val startDato: LocalDate? = LocalDate.now().minusMonths(6),
	val sluttDato: LocalDate? = LocalDate.now().plusMonths(12),
)

data class DeltakerMessage (
	val operation: String = "CREATED",
	val id: UUID = UUID.randomUUID(),
	val personIdent: String = (1..Long.MAX_VALUE).random().toString(),
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

data class VirksomhetMessage (
	val organisasjonsnummer: String = "999888777",
	val navn: String = "Virksomhetsnavn",
	val overordnetEnhetOrganisasjonsnummer: String? = "111222333",
)
