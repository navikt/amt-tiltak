package no.nav.amt.tiltak.data_publisher

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
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
import java.util.UUID

class DataPublisherServiceTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val kafkaTopicProperties = createTopicProperties()
	val dbHandler = DatabaseTestDataHandler(template)
	lateinit var kafkaProducerClient: KafkaProducerClient<String, String>
	val publishRepository = PublishRepository(template)

	lateinit var service: DataPublisherService

	val publishAndVerify = fun(id: UUID, type: DataPublishType, expected: Int) {
		service.publish(id, type)
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

	test("Ny Deltakerliste - Sendes") {
		val input = dbHandler.createDeltakerliste()
		publishAndVerify(input.id, DELTAKERLISTE, 1)
	}

	test("Deltakerliste to ganger uten endring - sendes en gang") {
		val input = dbHandler.createDeltakerliste()
		publishAndVerify(input.id, DELTAKERLISTE, 1)
		publishAndVerify(input.id, DELTAKERLISTE, 1)
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
	amtArrangorTopic = "",
	amtEndringsmeldingTopic = "",
	amtDeltakerlisteTopic = "",
	amtDeltakerTopic = "",
	amtArrangorAnsattTopic = ""
)
