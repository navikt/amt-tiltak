package no.nav.amt.tiltak.common.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue

object JsonUtils {

	val objectMapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.registerModule(JavaTimeModule())
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	inline fun <reified T> fromJsonString(jsonStr: String): T {
		return objectMapper.readValue(jsonStr)
	}

	inline fun <reified T> fromJsonNode(jsonNode: JsonNode): T {
		return objectMapper.treeToValue(jsonNode)
	}

	fun toJsonString(any: Any): String {
		return objectMapper.writeValueAsString(any)
	}

}
