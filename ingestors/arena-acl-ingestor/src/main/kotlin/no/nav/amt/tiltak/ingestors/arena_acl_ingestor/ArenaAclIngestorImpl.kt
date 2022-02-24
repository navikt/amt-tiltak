package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import no.nav.amt.tiltak.common.json.JsonUtils.getObjectMapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.UnknownMessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.GjennomforingProcessor
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArenaAclIngestorImpl(
	private val deltakerProcessor: DeltakerProcessor,
	private val gjennomforingProcessor: GjennomforingProcessor
) : ArenaAclIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaMessageValue(messageValue: String) {
		val unknownMessageWrapper = fromJson(messageValue, UnknownMessageWrapper::class.java)

		wrapIngestWithLog(unknownMessageWrapper) {
			when (unknownMessageWrapper.type) {
				"DELTAKER" -> {
					val deltakerPayload = getObjectMapper().treeToValue(unknownMessageWrapper.payload, DeltakerPayload::class.java)
					val deltakerMessage = toKnownMessageWrapper(deltakerPayload, unknownMessageWrapper)
					deltakerProcessor.processMessage(deltakerMessage)
				}
				"GJENNOMFORING" -> {
					val gjennomforingPayload = getObjectMapper().treeToValue(unknownMessageWrapper.payload, GjennomforingPayload::class.java)
					val gjennomforingMessage = toKnownMessageWrapper(gjennomforingPayload, unknownMessageWrapper)
					gjennomforingProcessor.processMessage(gjennomforingMessage)
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
