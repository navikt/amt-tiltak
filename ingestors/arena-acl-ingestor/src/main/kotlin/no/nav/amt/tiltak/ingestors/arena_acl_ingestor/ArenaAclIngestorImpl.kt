package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Deltaker
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Gjennomforing
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.UnknownMessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.GjennomforingProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.utils.JsonUtils.getObjectMapper
import org.springframework.stereotype.Service

@Service
class ArenaAclIngestorImpl(
	private val deltakerProcessor: DeltakerProcessor,
	private val gjennomforingProcessor: GjennomforingProcessor
) : ArenaAclIngestor {

	override fun ingestKafkaMessageValue(messageValue: String) {
		val unknownMessageWrapper = getObjectMapper().readValue(messageValue, UnknownMessageWrapper::class.java)

		when (unknownMessageWrapper.type) {
			"DELTAKER" -> {
				val deltakerPayload = getObjectMapper().treeToValue(unknownMessageWrapper.payload, Deltaker::class.java)
				val deltakerMessage = toKnownMessageWrapper(deltakerPayload, unknownMessageWrapper)
				deltakerProcessor.processMessage(deltakerMessage)
			}
			"GJENNOMFORING" -> {
				val gjennomforingPayload = getObjectMapper().treeToValue(unknownMessageWrapper.payload, Gjennomforing::class.java)
				val gjennomforingMessage = toKnownMessageWrapper(gjennomforingPayload, unknownMessageWrapper)
				gjennomforingProcessor.processMessage(gjennomforingMessage)
			}
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
