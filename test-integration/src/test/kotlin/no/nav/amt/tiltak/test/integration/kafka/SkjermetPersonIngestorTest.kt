package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockKontaktinformasjon
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SkjermetPersonIngestorTest: IntegrationTestBase() {

	@Autowired
	private lateinit var deltakerService: DeltakerService


	@BeforeEach
	fun before() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@AfterEach
	internal fun tearDown() {
		DbTestDataUtils.cleanDatabase(dataSource)
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
