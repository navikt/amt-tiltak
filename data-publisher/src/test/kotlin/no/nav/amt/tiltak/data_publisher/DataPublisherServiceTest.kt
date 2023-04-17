package no.nav.amt.tiltak.data_publisher

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.data_publisher.model.DataPublishType.ARRANGOR
import no.nav.amt.tiltak.data_publisher.model.DataPublishType.DELTAKERLISTE
import no.nav.amt.tiltak.data_publisher.publish.PublishRepository
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class DataPublisherServiceTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val kafkaTopicProperties = createTopicProperties()
	val dbHandler = DatabaseTestDataHandler(template)
	lateinit var kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>
	val publishRepository = PublishRepository(template)

	lateinit var service: DataPublisherService

	val publishAndVerify = fun(id: UUID, type: DataPublishType, expected: Int) {
		service.publish(id, type)
		verify(exactly = expected) { kafkaProducerClient.sendSync(any()) }
	}

	val publishAllAndVerify = fun(expected: Int) {
		service.publishAll()
		verify(exactly = expected) { kafkaProducerClient.sendSync(any()) }
	}

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)

		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.INFO

		kafkaProducerClient = mockk()

		every { kafkaProducerClient.sendSync(any()) } returns RecordMetadata(
			TopicPartition("", 0),
			0L,
			0,
			0L,
			0,
			0
		)

		service = DataPublisherService(
			kafkaTopicProperties, kafkaProducerClient, template, publishRepository
		)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	/**
	 * ---        TESTING         ---
	 * - Legge til Arrangor
	 * - Oppdatere Arrangor
	 * - Legge til ny Gjennomføring
	 *
	 * - Legge til ny Arrangor ansatt
	 * - Oppdatere Personalia
	 * - Oppdatere Tilknyttede Arrangorer
	 * - Oppdatere roller for arrangører
	 * - oppdatere veiledere
	 * - Oppdatere koordinatorer
	 *
	 * - Legge til ny Gjennomføring
	 * - Oppdatere Gjennomføring
	 *
	 * - Legge til ny deltaker
	 * - Oppdatere personalia
	 * - Oppdatere deltakerinformasjon
	 * - Oppdatere NAV-ansatt
	 *
	 * - Legge til ny endringsmelding
	 * - Oppdatere endringsmelding
	 * --- OPPRETTE TOPICS I PROD ---
	 * ---  OPPKOBLING MOT APPEN  ---
	 */


	test("Ny Arrangør - Sendes") {
		val input = dbHandler.createArrangor()
		dbHandler.createDeltakerliste(arrangorId = input.id)

		publishAndVerify(input.id, ARRANGOR, 1)
	}

	test("Samme arrangør to ganger uten endring - Sendes en gang") {
		val input = dbHandler.createArrangor()

		publishAndVerify(input.id, ARRANGOR, 1)
		publishAndVerify(input.id, ARRANGOR, 1)
	}

	test("Oppdatere eksisterende arrangør - sender oppdatert arrangør") {
		val input = dbHandler.createArrangor()
		publishAndVerify(input.id, ARRANGOR, 1)

		dbHandler.updateArrangor(input.copy(navn = "ENDRET"))
		publishAndVerify(input.id, ARRANGOR, 2)
	}

	test("publishAll - Sender arrangør hver gang den kalles") {
		dbHandler.createArrangor()
		publishAllAndVerify(1)
		publishAllAndVerify(2)
	}

	test("Ny Deltakerliste - Sendes") {
		val input = dbHandler.createDeltakerliste()
		publishAndVerify(input.id, DELTAKERLISTE, 1)
	}

	test("Deltakerliste to ganger uten endring - sendes en gang") {
		val input = dbHandler.createDeltakerliste()
		publishAndVerify(input.id, DELTAKERLISTE, 1)
		publishAndVerify(input.id, DELTAKERLISTE, 1)
	}

	test("Deltakerliste - oppdatert tiltaksnavn - sendes på nytt") {
		val tiltakInput = dbHandler.createTiltak()
		val gjennomforingInput = dbHandler.createDeltakerliste(tiltakId = tiltakInput.id)
		publishAndVerify(gjennomforingInput.id, DELTAKERLISTE, 1)

		dbHandler.updateTiltak(tiltakInput.copy(navn = "OPPDATERT"))
		publishAndVerify(gjennomforingInput.id, DELTAKERLISTE, 2)
	}

})

private fun createTopicProperties(): KafkaTopicProperties = KafkaTopicProperties(
	amtTiltakTopic = "",
	sisteTilordnetVeilederTopic = "",
	endringPaaBrukerTopic = "",
	skjermedePersonerTopic = "",
	sisteTiltaksgjennomforingerTopic = "",
	leesahTopic = "",
	deltakerTopic = "",
	aktorV2Topic = "",
	virksomhetTopic = "",
	amtArrangorTopic = "",
	amtEndringsmeldingTopic = "",
	amtDeltakerlisteTopic = "",
	amtDeltakerTopic = "",
	amtArrangorAnsattTopic = ""
)
