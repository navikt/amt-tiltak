package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.ingestors.amt_arrangor_ingestor.AmtArrangorIngestorImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.utils.AsyncUtils.eventually
import no.nav.common.json.JsonUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AmtArrangorIngestorTest : IntegrationTestBase() {


	@Autowired
	private lateinit var arrangorRepository: ArrangorRepository

	@BeforeEach
	fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ny arrangor - source er amt-tiltak - kaster meldingen`() {
		val dto = arrangorDto(source = "amt-tiltak")

		kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(dto))
		Thread.sleep(2000) //Må vente for å vente på at kafka meldingen blir lest.

		assertThrows<NoSuchElementException> { arrangorRepository.getById(dto.id) }
	}

	@Test
	fun `ny arrangor - ikke overordnet enhet - kaller ikke arrangorClient og lagrer data`() {
		val dto = arrangorDto()
			.also { kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(it)) }

		eventually { arrangorRepository.getByIder(listOf(dto.id)).size shouldNotBe 0 }
		mockArrangorServer.requestCount() shouldBe 0

	}

	@Test
	fun `ny arrangor - overordnet arrangør - kaller arrangorClient og lagrer data`() {
		val dto = arrangorDto(overordnetArranogrId = UUID.randomUUID())

		val clientResponse = clientResponse(dto)
			.also { mockArrangorServer.addArrangorResponse(it) }

		kafkaMessageSender.sendTilAmtArrangorTopic(JsonUtils.toJson(dto))

		eventually { arrangorRepository.getByIder(listOf(dto.id)).size shouldNotBe 0 }

		val arrangor = arrangorRepository.getById(dto.id)

		arrangor.id shouldBe dto.id
		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe clientResponse.overordnetArrangor?.organisasjonsnummer
		mockArrangorServer.requestCount() shouldBe 1
	}

	@Test
	fun `oppdatert arrangør - oppdaterer arrangørdata`() {
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
		dto: AmtArrangorIngestorImpl.ArrangorDto,
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
		source: String = "TEST",
		navn: String = UUID.randomUUID().toString(),
		organisasjonsnummer: String = UUID.randomUUID().toString(),
		overordnetArranogrId: UUID? = null
	) = AmtArrangorIngestorImpl.ArrangorDto(id, source, navn, organisasjonsnummer, overordnetArranogrId)
}
