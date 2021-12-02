package no.nav.amt.tools.mockdatagenerator.tiltaksleverandor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random.Default.nextInt

class GenerateTiltaksleverandorMockData {
	private val objectMapper = jacksonObjectMapper()

	private val arenaIds = getArenaIder()
	private val overordnedeEnheter = getOverordnedeEnheter()
	private val underEnehter = getUnderEnheter()

	fun generate() {
		val data = arenaIds.map { arenaId ->
			val overordnetEnhet = getEnhet()
			val virksomhet = getUnderenhet()

			MockedTiltaksleverandor(
				arenaId = arenaId,
				organisasjonsnummer = overordnetEnhet.organisasjonsnummer,
				organisasjonsnavn = overordnetEnhet.navn,
				virksomhetsnummer = virksomhet.organisasjonsnummer,
				virksomhetsnavn = virksomhet.navn
			)
		}

		val writer = objectMapper.writerWithDefaultPrettyPrinter()
		writer.writeValue(File("mocks/src/main/resources/tiltaksleverandor_mock_data.json"), data)
	}

	private fun getArenaIder(): Set<Long> {
		val fileReader = FileInputStream("tools/arena-kafka-producer/data/arena_tiltak/TILTAKGJENNOMFORING.json")
		val data: List<Tiltaksgjennomforing> = objectMapper.readValue(fileReader)

		return data.map { it.arbeidsgiverId }.filterNotNull().toSet()
	}

	private fun getEnhet(): Enhet {
		val element = overordnedeEnheter[nextInt(overordnedeEnheter.size)]
		overordnedeEnheter.remove(element)

		return element
	}

	private fun getUnderenhet(): Enhet {
		val element = underEnehter[nextInt(underEnehter.size)]
		underEnehter.remove(element)

		return element
	}

	private fun getOverordnedeEnheter(): MutableList<Enhet> {
		val data: List<Enhet> =
			objectMapper.readValue<List<Enhet>>(this.javaClass.classLoader.getResourceAsStream("enheter_alle.json"))
				.filter {
					it.organisasjonsform.kode == "AS"
				}

		return data.toMutableList()
	}

	private fun getUnderEnheter(): MutableList<Enhet> {
		val data: List<Enhet> =
			objectMapper.readValue<List<Enhet>>(this.javaClass.classLoader.getResourceAsStream("underenheter_alle.json"))
				.filter {
					it.organisasjonsform.kode == "BEDR"
				}

		return data.toMutableList()
	}

	data class MockedTiltaksleverandor(
		val arenaId: Long,
		val organisasjonsnummer: String,
		val organisasjonsnavn: String,
		val virksomhetsnummer: String,
		val virksomhetsnavn: String,
	)

	@JsonIgnoreProperties(ignoreUnknown = true)
	data class Enhet(
		val organisasjonsnummer: String,
		val navn: String,
		val organisasjonsform: Organisasjonsform,
		val naeringskode1: Naeringskode?,
		val naeringskode2: Naeringskode?
	)

	@JsonIgnoreProperties(ignoreUnknown = true)
	data class Organisasjonsform(
		val kode: String
	)

	data class Naeringskode(
		val beskrivelse: String,
		val kode: String,
		val hjelpeenhetskode: String?
	)

	@JsonIgnoreProperties(ignoreUnknown = true)
	data class Tiltaksgjennomforing(

		@JsonProperty("ARBGIV_ID_ARRANGOR")
		val arbeidsgiverId: Long?
	)

}
