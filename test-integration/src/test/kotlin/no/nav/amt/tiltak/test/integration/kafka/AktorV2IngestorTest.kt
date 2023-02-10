package no.nav.amt.tiltak.test.integration.kafka

import com.github.avrokotlin.avro4k.Avro
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AktorV2IngestorTest: IntegrationTestBase() {

	@Autowired
	lateinit var brukerRepository: BrukerRepository

	@Autowired
	lateinit var mockSchemaRegistryClient: MockSchemaRegistryClient

	lateinit var kafkaAvroSerializer: KafkaAvroSerializer

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()

		val schema = Avro.default.schema(AktorV2Payload.serializer())
		mockSchemaRegistryClient.register("aktorV2-value", schema)
		kafkaAvroSerializer = KafkaAvroSerializer(mockSchemaRegistryClient)
	}

	@Test
	fun `ingest - bruker finnes med gjeldende ident - skal ikke oppdatere bruker` () {
		val aktorV2Payload = constructPayload(BRUKER_1.personIdent)
		val record = Avro.default.toRecord(AktorV2Payload.serializer(), aktorV2Payload)
		val bytes =  kafkaAvroSerializer.serialize("aktorV2", record)

		kafkaMessageSender.sendTilAktorV2Topic("123", bytes)

		AsyncUtils.eventually {
			val bruker = brukerRepository.get(BRUKER_1.personIdent)
			bruker!!.historiskeIdenter shouldBe emptyList()
		}
	}

	@Test
	fun `ingest - bruker finnes med gjeldende ident, uten historiske identer - skal oppdatere bruker` () {
		val gammelIdent = "4738294"
		val aktorV2Payload = constructPayload(BRUKER_1.personIdent, listOf(gammelIdent))
		val record = Avro.default.toRecord(AktorV2Payload.serializer(), aktorV2Payload)
		val bytes =  kafkaAvroSerializer.serialize("aktorV2", record)

		kafkaMessageSender.sendTilAktorV2Topic("123", bytes)

		AsyncUtils.eventually {
			val bruker = brukerRepository.get(BRUKER_1.personIdent)
			bruker!!.historiskeIdenter shouldBe listOf(gammelIdent)
		}
	}

	@Test
	fun `ingest - bruker har utdatert ident - skal oppdatere alle felter` () {
		val nyGjeldendeIdent = "4738294"
		val aktorV2Payload = constructPayload(gjeldendeIdent = nyGjeldendeIdent, listOf(BRUKER_1.personIdent) )
		val record = Avro.default.toRecord(AktorV2Payload.serializer(), aktorV2Payload)
		val bytes =  kafkaAvroSerializer.serialize("aktorV2", record)

		kafkaMessageSender.sendTilAktorV2Topic("123", bytes)

		AsyncUtils.eventually {
			val bruker = brukerRepository.get(nyGjeldendeIdent)
			bruker!!.historiskeIdenter shouldBe listOf(BRUKER_1.personIdent)
		}
	}

	private fun constructPayload (gjeldendeIdent: String, personIdenter: List<String> = emptyList()) : AktorV2Payload {
		val identifikatorer = personIdenter.map { personIdent ->
			PersonIdent(
				idnummer = personIdent,
				type = Type.FOLKEREGISTERIDENT,
				gjeldende = false
			)
		}.plus(
			PersonIdent(
				idnummer = gjeldendeIdent,
				type = Type.FOLKEREGISTERIDENT,
				gjeldende = true
			)
		)

		return AktorV2Payload(identifikatorer)
	}

	@Serializable
	data class AktorV2Payload(
		val identifikatorer: List<PersonIdent>,
	)

	@Serializable
	data class PersonIdent(
		val idnummer: String,
		val type: Type,
		val gjeldende: Boolean
	)

	@Serializable
	enum class Type {
		FOLKEREGISTERIDENT, AKTORID, NPID
	}
}
