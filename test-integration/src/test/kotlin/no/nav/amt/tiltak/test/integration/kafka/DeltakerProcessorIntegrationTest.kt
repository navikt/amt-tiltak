package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockKontaktinformasjon
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.utils.AsyncUtils
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.integration.utils.LogUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeltakerProcessorIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var deltakerService: DeltakerService

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `skal inserte ny deltaker`() {
		val mockBruker = MockPdlBruker()

		val message = DeltakerMessage(gjennomforingId = GJENNOMFORING_1.id)

		mockVeilarboppfolgingHttpServer.mockHentVeilederIdent(message.personIdent, NAV_ANSATT_1.navIdent)
		mockVeilarbarenaHttpServer.mockHentBrukerOppfolgingsenhetId(message.personIdent, NAV_ENHET_1.enhetId)
		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(MockKontaktinformasjon("epost", "mobil"))
		mockPoaoTilgangHttpServer.addErSkjermetResponse(mapOf(message.personIdent to false))

		mockPdlHttpServer.mockHentBruker(message.personIdent, mockBruker)
		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val maybeDeltaker = deltakerService.hentDeltaker(message.id)

			maybeDeltaker shouldNotBe null

			val deltaker = maybeDeltaker!!
			deltaker.personIdent shouldBe message.personIdent
			deltaker.gjennomforingId shouldBe message.gjennomforingId
			deltaker.prosentStilling shouldBe 0.0 // message.prosentDeltid - NULL i databasen blir konvertert til 0.0
			deltaker.dagerPerUke shouldBe message.dagerPerUke
			deltaker.registrertDato shouldBeEqualTo message.registrertDato
			deltaker.startDato shouldBe message.startDato
			deltaker.sluttDato shouldBe message.sluttDato
			deltaker.status.type shouldBe message.status
			deltaker.status.aarsak shouldBe message.statusAarsak
			deltaker.innsokBegrunnelse shouldBe message.innsokBegrunnelse
			deltaker.fornavn shouldBe mockBruker.fornavn
			deltaker.etternavn shouldBe mockBruker.etternavn
		}

	}

	@Test
	fun `skal slette deltakere som er feilregistrert`() {

		testDataRepository.deleteAllEndringsmeldinger() // Trengs fordi slettDeltaker() sletter ikke endringsmeldinger knyttet til deltaker
		deltakerService.hentDeltaker(DELTAKER_1.id) shouldNotBe null

		val message = DeltakerMessage(id = DELTAKER_1.id, gjennomforingId = DELTAKER_1.gjennomforingId, status = DeltakerStatus.Type.FEILREGISTRERT)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.hentDeltaker(DELTAKER_1.id) shouldBe null
		}

	}

	@Test
	fun `skal ikke inserte deltaker med diskresjonskode`() {
		val message = DeltakerMessage(gjennomforingId = GJENNOMFORING_1.id)
		mockPdlHttpServer.mockHentBruker(message.personIdent, MockPdlBruker(adressebeskyttelse = AdressebeskyttelseGradering.FORTROLIG))


		LogUtils.withLogs { getLogs ->
			kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))
			AsyncUtils.eventually {
				getLogs().any { it.message == "Deltaker id=${message.id} har diskresjonskode KODE_7 og skal filtreres ut" } shouldBe true

				deltakerService.hentDeltaker(message.id) shouldBe null
			}
		}
	}

	@Test
	fun `skal slette deltaker med operation DELETED`() {

		testDataRepository.deleteAllEndringsmeldinger() // Trengs fordi slettDeltaker() sletter ikke endringsmeldinger knyttet til deltaker
		deltakerService.hentDeltaker(DELTAKER_1.id) shouldNotBe null

		val message = DeltakerMessage(id = DELTAKER_1.id, gjennomforingId = DELTAKER_1.gjennomforingId, operation = "DELETED")

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.hentDeltaker(DELTAKER_1.id) shouldBe null
		}


	}

}
