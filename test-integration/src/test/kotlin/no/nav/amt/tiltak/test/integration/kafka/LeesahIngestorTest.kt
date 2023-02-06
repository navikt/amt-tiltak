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


	@Test
	fun `skal slette deltakere med adressebeskyttelse`() {
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
	internal fun `Skal oppdatere brukeren om endringer`() {
		val expectedMellomnavn = "NYTT MELLOMNAVN"
		val expectedEpost = "test@testersen.no"
		val expectedTelefon = "+4787652154"

		val leesahData = LeesahData(
			personidenter = listOf(BRUKER_1.personIdent),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.UGRADERT
			),
			navn().copy(
				mellomnavn = expectedMellomnavn
			)
		)

		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(
			MockKontaktinformasjon(
				expectedEpost,
				expectedTelefon
			)
		)

		val record = Avro.default.toRecord(LeesahData.serializer(), leesahData)

		val bytes = kafkaAvroSerializer.serialize("leesah", record)
		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)

		AsyncUtils.eventually {
			val deltakere = deltakerService.hentDeltakereMedPersonIdent(BRUKER_1.personIdent)
			deltakere.size shouldBe 1
			val deltaker = deltakere.first()
			deltaker.mellomnavn shouldBe expectedMellomnavn
			deltaker.epost shouldBe expectedEpost
			deltaker.telefonnummer shouldBe expectedTelefon
		}
	}

	private fun navn() = Navn(
		fornavn = BRUKER_1.fornavn,
		mellomnavn = BRUKER_1.mellomnavn,
		etternavn = BRUKER_1.etternavn,
		forkortetNavn = null,
		originaltNavn = OriginaltNavn(
			fornavn = "Test",
			mellomnavn = null,
			etternavn = "Testersen"
		)
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
