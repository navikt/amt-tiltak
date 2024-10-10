package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageConsumer
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class KafkaProducerServiceImplIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var deltakerService: DeltakerService

	@Autowired
	lateinit var kafkaProducerService: KafkaProducerService

	@Autowired
	lateinit var kafkaMessageConsumer: KafkaMessageConsumer

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `skal publisere deltaker med riktig key og value`() {
		val deltaker = deltakerService.hentDeltaker(DELTAKER_1.id)!!
		kafkaProducerService.publiserDeltaker(deltaker, deltaker.endretDato)

		val expectedJson = """
			{"id":"dc600c70-124f-4fe7-a687-b58439beb214","gjennomforingId":"b3420940-5479-48c8-b2fa-3751c7a33aa2","personIdent":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","statusTekst":"Deltar","aarsak":null,"aarsakTekst":null,"opprettetDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","dagerPerUke":2.5,"prosentStilling":100.0,"endretDato":"2022-02-13T12:12:00","kilde":"ARENA"}
		""".trimIndent()

		AsyncUtils.eventually {
			val deltakerRecord = kafkaMessageConsumer.getLatestRecord(KafkaMessageConsumer.Topic.DELTAKER)
			deltakerRecord?.key() shouldBe DELTAKER_1.id.toString()
			deltakerRecord?.value() shouldBe expectedJson
		}
	}

	@Test
	fun `skal publisere sletting av deltaker med riktig key og null value`() {
		kafkaProducerService.publiserSlettDeltaker(DELTAKER_1.id)

		AsyncUtils.eventually {
			val deltakerRecord = kafkaMessageConsumer.getLatestRecord(KafkaMessageConsumer.Topic.DELTAKER)
			deltakerRecord?.key() shouldBe DELTAKER_1.id.toString()
			deltakerRecord?.value() shouldBe null
		}
	}

}
