package no.nav.amt.tiltak.mocks

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tiltak.common.json.JsonUtils

object ArrangorMockDataProvider {
	private val byArenaId: Map<Long, MockedArrangor>
	private val byVirksomhetsnummer: Map<String, MockedArrangor>

	init {

		val data: List<MockedArrangor> =
			JsonUtils.objectMapper.readValue(MockedArrangor::class.java.classLoader.getResourceAsStream("arrangor_mock_data.json"))

		byArenaId = data.associateBy { it.arenaId }
		byVirksomhetsnummer = data.associateBy { it.virksomhetsnummer }
	}

	fun getArrangorByArenaId(arenaId: Long): MockedArrangor? {
		return byArenaId[arenaId]
	}

	fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): MockedArrangor? {
		return byVirksomhetsnummer[virksomhetsnummer]
	}

}

data class MockedArrangor(
	val arenaId: Long,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
)

