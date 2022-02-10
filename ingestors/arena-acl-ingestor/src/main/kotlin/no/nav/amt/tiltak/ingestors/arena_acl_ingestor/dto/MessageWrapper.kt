package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

enum class Operation {
	CREATED,
	MODIFIED,
	DELETED
}

data class MessageWrapper<T>(
	val transactionId: String,
	val type: String,
	val timestamp: LocalDateTime,
	val operation: Operation,
	val payload: T
)

data class UnknownMessageWrapper(
	val transactionId: String,
	val type: String,
	val timestamp: LocalDateTime,
	val operation: Operation,
	val payload: JsonNode
)
