package no.nav.amt.tiltak.mocks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object TiltaksleverandorMockDataProvider {
	private val byArenaId: Map<Long, MockedTiltaksleverandor>
	private val byVirksomhetsnummer: Map<String, MockedTiltaksleverandor>

	init {
		val objectMapper = jacksonObjectMapper()

		val data: List<MockedTiltaksleverandor> =
			objectMapper.readValue(MockedTiltaksleverandor::class.java.classLoader.getResourceAsStream("tiltaksleverandor_mock_data.json"))

		byArenaId = data.associateBy { it.arenaId }
		byVirksomhetsnummer = data.associateBy { it.virksomhetsnummer }
	}

	fun getTiltaksleverandorByArenaId(arenaId: Long): MockedTiltaksleverandor? {
		return byArenaId[arenaId]
	}

	fun getTiltaksleverandorByVirksomhetsnummer(virksomhetsnummer: String): MockedTiltaksleverandor? {
		return byVirksomhetsnummer[virksomhetsnummer]
	}

}

data class MockedTiltaksleverandor(
	val arenaId: Long,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
)

