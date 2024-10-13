package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonNode
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.kafka.ArenaAclIngestor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.UnknownMessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.ArenaDeltakerProcessor
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArenaAclIngestorImpl(
	private val arenaDeltakerProcessor: ArenaDeltakerProcessor,
) : ArenaAclIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaRecord(recordValue: String) {
		val unknownMessageWrapper = fromJsonString<UnknownMessageWrapper>(recordValue)

		wrapIngestWithLog(unknownMessageWrapper) {
			when (unknownMessageWrapper.type) {
				"DELTAKER" -> {
					val deltakerPayload = fromJsonNode<DeltakerPayload>(unknownMessageWrapper.payload)
					val deltakerMessage = toKnownMessageWrapper(deltakerPayload, unknownMessageWrapper)
					arenaDeltakerProcessor.processMessage(deltakerMessage)
				}
				else -> {
					throw IllegalArgumentException("Ukjent meldingtype ${unknownMessageWrapper.type}")
				}
			}
		}
	}

	private fun wrapIngestWithLog(message: UnknownMessageWrapper, ingestor: (message: UnknownMessageWrapper) -> Unit) {
		try {
			val traceId = UUID.randomUUID()

			MDC.put("traceId", traceId.toString())

			log.info(
				"Processing arena message" +
				" transactionId=${message.transactionId}" +
				" traceId=$traceId" +
				" type=${message.type}" +
				" operation=${message.operation}" +
				" timestamp=${message.timestamp}"
			)

			ingestor.invoke(message)
		} finally {
			MDC.remove("traceId")
		}
	}

	private fun <T> toKnownMessageWrapper(payload: T, unknownMessageWrapper: UnknownMessageWrapper): MessageWrapper<T> {
		return MessageWrapper(
			transactionId = unknownMessageWrapper.transactionId,
			type = unknownMessageWrapper.type,
			timestamp = unknownMessageWrapper.timestamp,
			operation = unknownMessageWrapper.operation,
			payload = payload
		)
	}

}
