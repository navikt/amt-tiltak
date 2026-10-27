package no.nav.amt.tiltak.common.json

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.treeToValue

object JsonUtils {

	val objectMapper: ObjectMapper = jacksonMapperBuilder()
		.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.build()

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
