package no.nav.amt.tools.mockdatagenerator.person

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

class GeneratePersonMockData {
	private val objectMapper = jacksonObjectMapper()

	private val arenaIds = getArenaIds()
	private val fornavnList = getFile("fornavn")
	private val mellomnavnList = getFile("mellomnavn")
	private val etternavnList = getFile("etternavn")
	private val fodselsnummerList = getFile("fodselsnummer")

	companion object {
		private val PERCENTAGE_MELLOMNAVN = 0.20
		private val PERCENTAGE_TELEFONNUMMER = 0.80
	}

	fun generate() {
		val data = mutableSetOf<MockPersonData>()

		this.arenaIds.forEach { arenaId ->
			data.add(
				MockPersonData(
					arenaId = arenaId,
					fodselsnummer = getFodselsnummer(),
					fornavn = getFornavn(),
					mellomnavn = getMellomnavn(),
					etternavn = getEtternavn(),
					telefonnummer = getTelefonnummer()
				)
			)
		}

		val writer = objectMapper.writerWithDefaultPrettyPrinter()
		writer.writeValue(File("mocks/src/main/resources/person_mock_data.json"), data)

		println()
	}

	private fun getFornavn(): String {
		return fornavnList[nextInt(fornavnList.size)]
	}

	private fun getMellomnavn(): String? {
		val chance = nextDouble(1.0)

		return if (chance < PERCENTAGE_MELLOMNAVN) {
			mellomnavnList[nextInt(mellomnavnList.size)]
		} else {
			null
		}
	}

	private fun getEtternavn(): String {
		return etternavnList[nextInt(etternavnList.size)]
	}

	private fun getTelefonnummer(): String? {
		val chance = nextDouble(1.0)

		return if (chance < PERCENTAGE_TELEFONNUMMER) {
			nextInt(40000000, 50000000).toString()
		} else {
			null
		}


	}

	private fun getFodselsnummer(): String {
		if (fodselsnummerList.isEmpty()) {
			throw UnsupportedOperationException("Ikke nok fødselsnummer, generer fler på https://norske-testdata.no/fnr/")
		}

		val index = nextInt(fodselsnummerList.size)

		val element = fodselsnummerList[index]
		this.fodselsnummerList.remove(element)

		return element
	}

	private fun getArenaIds(): Set<Long> {
		val fileReader = FileInputStream("tools/arena-kafka-producer/data/arena_tiltak/TILTAKDELTAKER.json")
		val data: List<ArenaData> = objectMapper.readValue(fileReader)

		return data.map { it.personId }.toSet()
	}

	private fun getFile(name: String): MutableList<String> {
		val fileReader = FileInputStream("tools/mock-data-generator/src/main/resources/$name.json")
		return objectMapper.readValue(fileReader)
	}


	@JsonIgnoreProperties(ignoreUnknown = true)
	data class ArenaData(
		@JsonProperty("PERSON_ID")
		val personId: Long
	)

	data class MockPersonData(
		val arenaId: Long,
		val fodselsnummer: String,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val telefonnummer: String?
	)
}
