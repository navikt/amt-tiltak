package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockKontaktinformasjon
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.GjennomforingMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.integration.utils.LogUtils
import no.nav.amt.tiltak.test.utils.AsyncUtils
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class DeltakerProcessorIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var deltakerService: DeltakerService

	@Autowired
	lateinit var gjennomforingRepository: GjennomforingRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ingest deltaker - gjennomforing er ingestet`() {
		val mockBruker = MockPdlBruker()
		val gjennomforing = ingestGjennomforing()
		val message = DeltakerMessage(gjennomforingId = gjennomforing.id)

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
			deltaker.erSkjermet shouldBe false
			deltaker.fornavn shouldBe mockBruker.fornavn
			deltaker.etternavn shouldBe mockBruker.etternavn
		}

	}

	@Test
	fun `ingest deltaker - deltaker er skjermet - skal inserte ny deltaker med skjermingsdata `() {
		val mockBruker = MockPdlBruker()

		val message = DeltakerMessage(gjennomforingId = GJENNOMFORING_1.id)

		mockVeilarboppfolgingHttpServer.mockHentVeilederIdent(message.personIdent, NAV_ANSATT_1.navIdent)
		mockVeilarbarenaHttpServer.mockHentBrukerOppfolgingsenhetId(message.personIdent, NAV_ENHET_1.enhetId)
		mockDkifHttpServer.mockHentBrukerKontaktinformasjon(MockKontaktinformasjon("epost", "mobil"))
		mockPoaoTilgangHttpServer.addErSkjermetResponse(mapOf(message.personIdent to true))

		mockPdlHttpServer.mockHentBruker(message.personIdent, mockBruker)
		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val maybeDeltaker = deltakerService.hentDeltaker(message.id)

			maybeDeltaker shouldNotBe null

			val deltaker = maybeDeltaker!!
			deltaker.personIdent shouldBe message.personIdent
			deltaker.gjennomforingId shouldBe message.gjennomforingId
			deltaker.erSkjermet shouldBe true
			deltaker.fornavn shouldBe mockBruker.fornavn
			deltaker.etternavn shouldBe mockBruker.etternavn
		}

	}

	@Test
	fun `ingest deltaker - status feilregistrert - skal slette deltaker`() {
		deltakerService.hentDeltaker(DELTAKER_1.id) shouldNotBe null

		val message = DeltakerMessage(id = DELTAKER_1.id, gjennomforingId = DELTAKER_1.gjennomforingId, status = DeltakerStatus.Type.FEILREGISTRERT)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.hentDeltaker(DELTAKER_1.id) shouldBe null
		}

	}

	@Test
	fun `ingest deltaker - deltaker har diskresjonskode - skal ikke insertes`() {
		val message = DeltakerMessage(gjennomforingId = GJENNOMFORING_1.id)
		mockPdlHttpServer.mockHentBruker(message.personIdent, MockPdlBruker(adressebeskyttelse = AdressebeskyttelseGradering.FORTROLIG))


		LogUtils.withLogs { getLogs ->
			kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))
			AsyncUtils.eventually {
				getLogs().any { it.message == "Deltaker har diskresjonskode KODE_7 og skal filtreres ut" } shouldBe true

				deltakerService.hentDeltaker(message.id) shouldBe null
			}
		}
	}

	@Test
	fun `skal slette deltaker med operation DELETED`() {
		deltakerService.hentDeltaker(DELTAKER_1.id) shouldNotBe null

		val message = DeltakerMessage(id = DELTAKER_1.id, gjennomforingId = DELTAKER_1.gjennomforingId, operation = "DELETED")

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.hentDeltaker(DELTAKER_1.id) shouldBe null
		}
	}

	@Test
	fun `deltaker som er tidligere skjult skal oppheves når deltakerstatus oppdateres`() {
		val skjultDeltaker = DELTAKER_1.copy(id = UUID.randomUUID())

		testDataRepository.insertDeltaker(skjultDeltaker)
		testDataRepository.insertDeltakerStatus(TestData.DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = skjultDeltaker.id, status = "IKKE_AKTUELL"))

		deltakerService.skjulDeltakerForTiltaksarrangor(skjultDeltaker.id, ARRANGOR_ANSATT_1.id)

		deltakerService.erSkjultForTiltaksarrangor(skjultDeltaker.id) shouldBe true

		mockPdlHttpServer.mockHentBruker(BRUKER_1.personIdent, MockPdlBruker())

		val message = DeltakerMessage(
			id = skjultDeltaker.id,
			gjennomforingId = skjultDeltaker.gjennomforingId,
			status = DeltakerStatus.Type.DELTAR,
			personIdent = BRUKER_1.personIdent
		)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.erSkjultForTiltaksarrangor(skjultDeltaker.id) shouldBe false
		}
	}


	private fun ingestGjennomforing(): GjennomforingMessage {
		val enhetsregisterEnhet = EnhetDto(
			organisasjonsnummer = "999888777",
			navn = "Arrangor",
			overordnetEnhetNavn = "Arrangor Org",
			overordnetEnhetOrganisasjonsnummer = "888666555",
		)

		val gjennomforingArenaData = GjennomforingArenaData(
			opprettetAar = 2022,
			lopenr = 123,
			virksomhetsnummer = enhetsregisterEnhet.organisasjonsnummer,
			ansvarligNavEnhetId = "58749854",
			status = "GJENNOMFOR",
		)

		val gjennomforingMessage = GjennomforingMessage()
		val jsonObjekt = KafkaMessageCreator.opprettGjennomforingMessage(gjennomforingMessage)

		mockMulighetsrommetApiServer.gjennomforingArenaData(gjennomforingMessage.id, gjennomforingArenaData)

		mockEnhetsregisterServer.addEnhet(enhetsregisterEnhet)

		mockNorgHttpServer.addNavEnhet(gjennomforingArenaData.ansvarligNavEnhetId, "navEnhetNavn")

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
			val maybeGjennomforing = gjennomforingRepository.get(gjennomforingMessage.id)
			maybeGjennomforing shouldNotBe null
		}
		return gjennomforingMessage
	}
}
