package no.nav.amt.tiltak.mocks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object PersonMockDataProvider {
	private val byArenaId: Map<Long, PersonMockData>
	private val byFodselsnummer: Map<String, PersonMockData>


	init {
		val objectMapper = jacksonObjectMapper()

		val data: List<PersonMockData> =
			objectMapper.readValue(PersonMockDataProvider::class.java.classLoader.getResourceAsStream("person_mock_data.json"))

		byArenaId = data.associateBy { it.arenaId }
		byFodselsnummer = data.associateBy { it.fodselsnummer }
	}

	fun getPersonByArenaId(arenaId: Long): PersonMockData? {
		return byArenaId[arenaId]
	}

	fun getPersonByFodselsnummer(fodselsnummer: String): PersonMockData? {
		return byFodselsnummer[fodselsnummer]
	}

}

data class PersonMockData(
	val arenaId: Long,
	val fodselsnummer: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?
)
