package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.AsyncUtils
import no.nav.amt.tiltak.test.integration.utils.GjennomforingMessagePayload
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.integration.utils.LogUtils
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


class GjennomforingIngestorTest : IntegrationTestBase() {

	@Autowired
	lateinit var gjennomforingRepository: GjennomforingRepository

	@Autowired
	lateinit var tiltakRepository: TiltakRepository

	@Autowired
	lateinit var arrangorService: ArrangorService

	@Autowired
	lateinit var navEnhetService: NavEnhetService

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	val arrangorNavn = "Arrangor"
	val overordnetEnhetNavn = "Arrangor Org"
	val overordnetEnhetOrgNr = "888666555"
	val virksomhetsnummer = "999888777"

	val navEnhetId = "9876"
	val navEnhetNavn = "Nav Enhet"

	val gjennomforingArenaData = GjennomforingArenaData(
		opprettetAar = 2022,
		lopenr = 123,
		virksomhetsnummer = virksomhetsnummer,
		ansvarligNavEnhetId = navEnhetId,
		status = "GJENNOMFOR",
	)

	val payload = GjennomforingMessagePayload()
	val jsonObjekt = KafkaMessageCreator.opprettGjennomforingMessage(payload)


	@Test
	internal fun `skal inserte gjennomforing`() {

		mockMulighetsrommetApiServer.gjennomforingArenaData(payload.id, gjennomforingArenaData)

		mockEnhetsregisterServer.addEnhet(EnhetDto(
			organisasjonsnummer = virksomhetsnummer,
			navn = arrangorNavn,
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrgNr,
		))

		mockNorgHttpServer.addNavEnhet(navEnhetId, navEnhetNavn)

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
				val maybeGjennomforing = gjennomforingRepository.get(payload.id)
				maybeGjennomforing shouldNotBe null

				val gjennomforing = maybeGjennomforing!!

				gjennomforing.id shouldBe payload.id
				gjennomforing.navn shouldBe payload.navn
				gjennomforing.startDato shouldBe payload.startDato
				gjennomforing.sluttDato shouldBe payload.sluttDato
				gjennomforing.tiltakId shouldBe payload.tiltakId

				gjennomforing.opprettetAar shouldBe gjennomforingArenaData.opprettetAar
				gjennomforing.lopenr shouldBe gjennomforingArenaData.lopenr
				gjennomforing.status shouldBe Gjennomforing.Status.GJENNOMFORES

				val tiltak = tiltakRepository.getAll().find { it.id == payload.tiltakId }!!
				tiltak.navn shouldBe payload.tiltakNavn
				tiltak.type shouldBe payload.tiltakArenaKode

				val arrangor = arrangorService.getArrangorById(gjennomforing.arrangorId)
				arrangor.organisasjonsnummer shouldBe virksomhetsnummer
				arrangor.navn shouldBe arrangorNavn
				arrangor.overordnetEnhetNavn shouldBe overordnetEnhetNavn
				arrangor.overordnetEnhetOrganisasjonsnummer shouldBe overordnetEnhetOrgNr

				val navEnhet = navEnhetService.getNavEnhet(gjennomforing.navEnhetId!!)
				navEnhet.enhetId shouldBe navEnhetId
				navEnhet.navn shouldBe navEnhetNavn
			}
		 }
	@Test
	internal fun `skal ikke inserte gjennomforing uten virksomhetsnummer`() {

		mockMulighetsrommetApiServer.gjennomforingArenaData(payload.id, gjennomforingArenaData.copy(virksomhetsnummer = null))

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
			mockMulighetsrommetApiServer.requestCount() shouldBe 1
			gjennomforingRepository.get(payload.id) shouldBe null
		}
	}

	@Test
	internal fun `mottar melding om sletting - skal slette gjennomføring`() {
		val id = UUID.randomUUID()
		testDataRepository.insertGjennomforing(GJENNOMFORING_1.copy(id = id))

		kafkaMessageSender.sendDeleteTilSisteTiltaksgjennomforingTopic(id.toString())

		AsyncUtils.eventually {
			gjennomforingRepository.get(id) shouldBe null
		}
	}

	@Test
	internal fun `mottar gjennomforing som ikke er stottet - skal ikke inserte gjennomføring`() {
		val payloadIkkeStottet = payload.copy(
			tiltakId = UUID.randomUUID(),
			tiltakNavn = "Individuell jobbstotte 1",
			tiltakArenaKode = "INDJOBSTOT",
			navn = "Individuell jobbstotte 1",
		)

		LogUtils.withLogs { getLogs ->
			kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(KafkaMessageCreator.opprettGjennomforingMessage(payloadIkkeStottet))

			AsyncUtils.eventually {
				getLogs().any { it.message == "Lagrer ikke gjennomføring med id ${payloadIkkeStottet.id} og tiltakstype INDJOBSTOT fordi tiltaket ikke er støttet." } shouldBe true
			}
		}

	}
}
