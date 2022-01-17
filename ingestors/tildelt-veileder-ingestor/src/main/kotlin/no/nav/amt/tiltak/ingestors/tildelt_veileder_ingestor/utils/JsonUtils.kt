package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.utils

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

}
