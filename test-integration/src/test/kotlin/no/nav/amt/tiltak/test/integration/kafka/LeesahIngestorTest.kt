package no.nav.amt.tiltak.test.integration.kafka

import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.io.AvroInputStream
import com.github.avrokotlin.avro4k.io.AvroOutputStream
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.Serializable
import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockKontaktinformasjon
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.utils.AsyncUtils
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import org.apache.avro.Schema
import org.apache.avro.Schema.Field
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecordBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

class LeesahIngestorTest: IntegrationTestBase() {

	@Autowired
	private lateinit var deltakerService: DeltakerService


	@BeforeEach
	fun before() {
		//DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)

	}

	@Serializable
	data class LeesahData(
		val personidenter: Array<String>,
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
	@Test
	fun test() {
		val leesahData = LeesahData(
			personidenter = arrayOf("534534"),
			adressebeskyttelse = Adressebeskyttelse(
				gradering = Adressebeskyttelse.Gradering.STRENGT_FORTROLIG
			)
		)
		val schema = Avro.default.schema(LeesahData.serializer())
		val recordBuilder = GenericRecordBuilder(schema)
		recordBuilder.set("personidenter", leesahData.personidenter)
		recordBuilder.set("adressebeskyttelse", leesahData.adressebeskyttelse)

		val mockSchemaRegistryClient = MockSchemaRegistryClient()
		mockSchemaRegistryClient.register("test", schema)
		val kafkaAvroSerializer = KafkaAvroSerializer(mockSchemaRegistryClient)
		val bytes = kafkaAvroSerializer.serialize("test", recordBuilder.build())
		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)

	}
	/*
	@Test
	fun `ingest - deltakere finnes - skal slette deltakere`() {
		//val deltaker = ingestDeltaker()
		//val field = Field(Schema.Type.STRING, )
		val recordschema = Schema.createRecord("PersonhendelseProto", "doc", "ns", false)
		recordschema.fields = listOf()
		val data = GenericData.Record(recordschema)
		val adressebeskyttelse = GenericData.Record(Schema.create(Schema.Type.BYTES))
		adressebeskyttelse.put("gradering", "STRENGT_FORTROLIG")
		data.put("personidenter", GenericData.Array(Schema.create(Schema.Type.ARRAY), listOf("deltaker.personIdent")))
		data.put("adressebeskyttelse", adressebeskyttelse)
		val kafkaAvroSerializer = KafkaAvroSerializer()
		val bytes = kafkaAvroSerializer.serialize("", data)

		kafkaMessageSender.sendTilLeesahTopic("574839574", bytes)


	}*/

	private fun ingestDeltaker() : DeltakerMessage {
		val mockBruker = MockPdlBruker()
		val message = DeltakerMessage(gjennomforingId = TestData.GJENNOMFORING_1.id)

		mockVeilarboppfolgingHttpServer.mockHentVeilederIdent(message.personIdent, TestData.NAV_ANSATT_1.navIdent)
		mockVeilarbarenaHttpServer.mockHentBrukerOppfolgingsenhetId(message.personIdent, TestData.NAV_ENHET_1.enhetId)
		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(MockKontaktinformasjon("epost", "mobil"))
		mockPoaoTilgangHttpServer.addErSkjermetResponse(mapOf(message.personIdent to false))

		mockPdlHttpServer.mockHentBruker(message.personIdent, mockBruker)
		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val deltaker = deltakerService.hentDeltaker(message.id) ?: throw Exception("Fant ikke deltaker ${message.id}")

			deltaker shouldNotBe null

		}

		return message

	}
}
