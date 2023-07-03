package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.kafka.arrangor_ingestor.ArrangorIngestorImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.utils.AsyncUtils.eventually
import no.nav.common.json.JsonUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class ArrangorIngestorTest : IntegrationTestBase() {
	@Autowired
	private lateinit var arrangorRepository: ArrangorRepository

	@BeforeEach
	fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ingestArrangor - source er null - kaster meldingen`() {
		val dto = arrangorDto()

		kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(dto))
		Thread.sleep(2000) //Må vente for å vente på at kafka meldingen blir lest.

		assertThrows<NoSuchElementException> { arrangorRepository.getById(dto.id) }
	}

	@Test
	fun `ingestArrangor - ikke overordnet enhet - kaller ikke arrangorClient og lagrer data`() {
		val dto = arrangorDto(source = "amt-arrangor")
			.also { kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(it)) }

		eventually { arrangorRepository.getById(dto.id) shouldNotBe null }
		mockArrangorServer.requestCount() shouldBe 0

	}

	@Test
	fun `ingestArrangor - overordnet arrangor - kaller arrangorClient og lagrer data`() {
		val dto = arrangorDto(source = "amt-arrangor", overordnetArrangorId = UUID.randomUUID())

		val clientResponse = clientResponse(dto)
			.also { mockArrangorServer.addArrangorResponse(it) }

		kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(dto))

		eventually { arrangorRepository.getById(dto.id) shouldNotBe null }

		val arrangor = arrangorRepository.getById(dto.id)

		arrangor.id shouldBe dto.id
		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe clientResponse.overordnetArrangor?.organisasjonsnummer
		mockArrangorServer.requestCount() shouldBe 1
	}

	@Test
	fun `ingestArrangor - arrangor finnes fra for - oppdaterer arrangordata`() {
		val id = UUID.randomUUID()
		val organisasjonsnummer = UUID.randomUUID().toString()

		arrangorRepository.upsert(
			id = id,
			navn = "ORIGINAL",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetNavn = "ORIGINAL",
			overordnetEnhetOrganisasjonsnummer = UUID.randomUUID().toString()
		)

		val dto = arrangorDto(
			id = id,
			source = "amt-arrangor",
			organisasjonsnummer = organisasjonsnummer
		)
			.also { kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(it)) }

		eventually {
			arrangorRepository.getById(id).navn shouldBe dto.navn
		}

		val arrangor = arrangorRepository.getById(id)

		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe null
		arrangor.overordnetEnhetNavn shouldBe null
	}

	private fun clientResponse(
		dto: ArrangorIngestorImpl.ArrangorDto,
		overordnetArrangorId: UUID = UUID.randomUUID(),
		overordnetArrangorNavn: String = UUID.randomUUID().toString(),
		overordnetArrangorOrganisasjonsnummer: String = UUID.randomUUID().toString()
	): AmtArrangorClient.ArrangorMedOverordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
		id = dto.id,
		navn = dto.navn,
		organisasjonsnummer = dto.organisasjonsnummer,
		overordnetArrangor = AmtArrangorClient.Arrangor(
			id = overordnetArrangorId,
			navn = overordnetArrangorNavn,
			organisasjonsnummer = overordnetArrangorOrganisasjonsnummer
		)
	)

	private fun arrangorDto(
		id: UUID = UUID.randomUUID(),
		source: String? = null,
		navn: String = UUID.randomUUID().toString(),
		organisasjonsnummer: String = UUID.randomUUID().toString(),
		overordnetArrangorId: UUID? = null
	) = ArrangorIngestorImpl.ArrangorDto(
		id = id,
		source = source,
		navn = navn,
		organisasjonsnummer = organisasjonsnummer,
		overordnetArrangorId = overordnetArrangorId
	)
}
