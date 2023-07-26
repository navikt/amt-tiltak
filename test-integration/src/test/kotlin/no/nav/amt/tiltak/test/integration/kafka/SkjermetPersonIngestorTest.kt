package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.mockNavBruker
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class SkjermetPersonIngestorTest: IntegrationTestBase() {

	@Autowired
	private lateinit var deltakerService: DeltakerService


	@BeforeEach
	fun before() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ingest - deltaker finnes - skal oppdatere med skjermingsdata`() {
		val deltaker = ingestDeltaker()
		kafkaMessageSender.sendTilSkjermetPersonTopic(deltaker.personIdent, true)

		AsyncUtils.eventually {
			val deltakerSkjermet = deltakerService.hentDeltaker(deltaker.id)
			deltakerSkjermet!!.erSkjermet shouldBe true
		}

	}

	private fun ingestDeltaker() : DeltakerMessage {
		val message = DeltakerMessage(gjennomforingId = TestData.GJENNOMFORING_1.id)

		mockAmtPersonHttpServer.addNavBrukerResponse(
			mockNavBruker(
				BRUKER_1.copy(
					id = UUID.randomUUID(),
					personIdent = message.personIdent,
				),
				NAV_ENHET_1
			)
		)
		mockAmtPersonHttpServer.addAdressebeskyttelseResponse(message.personIdent, null)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val deltaker = deltakerService.hentDeltaker(message.id) ?: throw Exception("Fant ikke deltaker ${message.id}")

			deltaker shouldNotBe null

		}

		return message

	}
}
