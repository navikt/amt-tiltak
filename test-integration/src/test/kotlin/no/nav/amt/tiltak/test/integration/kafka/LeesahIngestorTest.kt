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
import no.nav.amt.tiltak.test.integration.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class LeesahIngestorTest : IntegrationTestBase() {

	@Autowired
	lateinit var mockSchemaRegistryClient: MockSchemaRegistryClient

	@Autowired
	lateinit var deltakerService: DeltakerService

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testDataRepository.deleteAllEndringsmeldinger()
	}


	@Test
	fun `skal slette deltakere med adressebeskyttelse`() {
		val leesahData = LeesahData(
			personidenter = listOf(BRUKER_1.personIdent),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.STRENGT_FORTROLIG
			)
		)
		val schema = Avro.default.schema(LeesahData.serializer())
		val record = Avro.default.toRecord(LeesahData.serializer(), leesahData)

		mockSchemaRegistryClient.register("leesah-value", schema)
		val kafkaAvroSerializer = KafkaAvroSerializer(mockSchemaRegistryClient)

		val bytes = kafkaAvroSerializer.serialize("leesah", record)
		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)

		AsyncUtils.eventually {
			val deltakere = deltakerService.hentDeltakereMedPersonIdent(BRUKER_1.personIdent)
			deltakere.isEmpty() shouldBe true
		}
	}

	@Serializable
	data class LeesahData(
		val personidenter: List<String>,
		val adressebeskyttelse: Adressebeskyttelse?
	)

	@Serializable
	data class Adressebeskyttelse (
		val gradering: Gradering
	) {
		enum class Gradering {
			STRENGT_FORTROLIG_UTLAND,
			STRENGT_FORTROLIG,
			FORTROLIG,
			UGRADERT
		}
	}
}
