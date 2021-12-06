package no.nav.amt.tiltak.mocks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object TiltaksarrangorMockDataProvider {
	private val byArenaId: Map<Long, MockedTiltaksarrangor>
	private val byVirksomhetsnummer: Map<String, MockedTiltaksarrangor>

	init {
		val objectMapper = jacksonObjectMapper()

		val data: List<MockedTiltaksarrangor> =
			objectMapper.readValue(MockedTiltaksarrangor::class.java.classLoader.getResourceAsStream("tiltaksleverandor_mock_data.json"))

		byArenaId = data.associateBy { it.arenaId }
		byVirksomhetsnummer = data.associateBy { it.virksomhetsnummer }
	}

	fun getTiltaksarrangorByArenaId(arenaId: Long): MockedTiltaksarrangor? {
		return byArenaId[arenaId]
	}

	fun getTiltaksarrangorByVirksomhetsnummer(virksomhetsnummer: String): MockedTiltaksarrangor? {
		return byVirksomhetsnummer[virksomhetsnummer]
	}

}

data class MockedTiltaksarrangor(
	val arenaId: Long,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
)

