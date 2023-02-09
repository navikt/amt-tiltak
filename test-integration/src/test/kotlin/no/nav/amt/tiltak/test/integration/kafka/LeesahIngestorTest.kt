package no.nav.amt.tiltak.test.integration.kafka

import com.github.avrokotlin.avro4k.Avro
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockKontaktinformasjon
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class LeesahIngestorTest : IntegrationTestBase() {

	@Autowired
	lateinit var mockSchemaRegistryClient: MockSchemaRegistryClient

	@Autowired
	lateinit var deltakerService: DeltakerService

	lateinit var kafkaAvroSerializer: KafkaAvroSerializer

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testDataRepository.deleteAllEndringsmeldinger()


		val schema = Avro.default.schema(LeesahData.serializer())
		mockSchemaRegistryClient.register("leesah-value", schema)

		kafkaAvroSerializer = KafkaAvroSerializer(mockSchemaRegistryClient)
	}

	@AfterEach
	internal fun tearDown() {
		DbTestDataUtils.cleanDatabase(dataSource)
		mockDkifHttpServer.resetHttpServer()
	}

	@Test
	fun `skal slette deltakere med adressebeskyttelse`() {
		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(
			MockKontaktinformasjon(
				BRUKER_1.epost,
				BRUKER_1.telefonnummer
			)
		)


		val leesahData = LeesahData(
			personidenter = listOf(BRUKER_1.personIdent),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.STRENGT_FORTROLIG
			),
			navn()
		)
		val record = Avro.default.toRecord(LeesahData.serializer(), leesahData)

		val bytes = kafkaAvroSerializer.serialize("leesah", record)
		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)

		AsyncUtils.eventually {
			val deltakere = deltakerService.hentDeltakereMedPersonIdent(BRUKER_1.personIdent)
			deltakere.isEmpty() shouldBe true
		}
	}

	@Test
	internal fun `Ingest - bruker finnes - oppdaterer navn, epost og telefon`() {
		val expectedFornavn = "NYTT FORNAVN"
		val expectedMellomnavn = "NYTT MELLOMNAVN"
		val expectedEtternavn = "NYTT ETTERNAVN"

		val expectedEpost = "ny@epost.no"
		val expectedTelefon = "+12345678"

		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(
			MockKontaktinformasjon(
				expectedEpost,
				expectedTelefon
			)
		)


		val leesahData = LeesahData(
			personidenter = listOf(BRUKER_1.personIdent),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.UGRADERT
			),
			navn().copy(
				fornavn = expectedFornavn,
				mellomnavn = expectedMellomnavn,
				etternavn = expectedEtternavn,
			)
		)

		val record = Avro.default.toRecord(LeesahData.serializer(), leesahData)

		val bytes = kafkaAvroSerializer.serialize("leesah", record)

		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)


		AsyncUtils.eventually {
			val deltakere = deltakerService.hentDeltakereMedPersonIdent(BRUKER_1.personIdent)
			deltakere.size shouldBe 1
			val deltaker = deltakere.first()
			deltaker.fornavn shouldBe expectedFornavn
			deltaker.mellomnavn shouldBe expectedMellomnavn
			deltaker.etternavn shouldBe expectedEtternavn
			deltaker.epost shouldBe expectedEpost
			deltaker.telefonnummer shouldBe expectedTelefon
		}
	}

	@Test
	internal fun `Ingest - bruker finnes - navn i melding = null skal fortsatt sjekke adressebeskyttelse`() {
		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(
			MockKontaktinformasjon(
				BRUKER_1.epost,
				BRUKER_1.telefonnummer
			)
		)


		val leesahData = LeesahData(
			personidenter = listOf(BRUKER_1.personIdent),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.STRENGT_FORTROLIG
			),
			navn = null
		)
		val record = Avro.default.toRecord(LeesahData.serializer(), leesahData)

		val bytes = kafkaAvroSerializer.serialize("leesah", record)
		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)

		AsyncUtils.eventually {
			val deltakere = deltakerService.hentDeltakereMedPersonIdent(BRUKER_1.personIdent)
			deltakere.isEmpty() shouldBe true
		}
	}


	private fun navn() = Navn(
		fornavn = BRUKER_1.fornavn,
		mellomnavn = BRUKER_1.mellomnavn,
		etternavn = BRUKER_1.etternavn,
		forkortetNavn = null,
		originaltNavn = null
	)

	@Serializable
	data class LeesahData(
		val personidenter: List<String>,
		val adressebeskyttelse: Adressebeskyttelse?,
		val navn: Navn?
	)

	@Serializable
	data class Adressebeskyttelse(
		val gradering: Gradering
	) {
		enum class Gradering {
			STRENGT_FORTROLIG_UTLAND,
			STRENGT_FORTROLIG,
			FORTROLIG,
			UGRADERT
		}
	}

	@Serializable
	data class Navn(
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val forkortetNavn: String?,
		val originaltNavn: OriginaltNavn?
	)

	@Serializable
	data class OriginaltNavn(
		val fornavn: String?,
		val mellomnavn: String?,
		val etternavn: String?
	)
}
