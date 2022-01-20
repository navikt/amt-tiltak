package no.nav.amt.tiltak.common.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {

	private val objectMapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.registerModule(JavaTimeModule())
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	fun getObjectMapper(): ObjectMapper {
		return objectMapper
	}

	fun <T> fromJson(jsonStr: String, clazz: Class<T>): T {
		return objectMapper.readValue(jsonStr, clazz)
	}

	fun toJson(any: Any): String {
		return objectMapper.writeValueAsString(any)
	}

}
