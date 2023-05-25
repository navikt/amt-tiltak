package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.GjennomforingMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.integration.utils.LogUtils
import no.nav.amt.tiltak.test.utils.AsyncUtils
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.UUID


class GjennomforingIngestorIntegrationTest : IntegrationTestBase() {

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

	val navEnhetNavn = "Nav Enhet"

	val overordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
		id = UUID.randomUUID(),
		organisasjonsnummer = "888666555",
		navn = "Arrangor Org",
		overordnetArrangor = null
	)
	val arrangorMedOverordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
		id = UUID.randomUUID(),
		organisasjonsnummer = "999888777",
		navn = "Arrangor",
		overordnetArrangor = AmtArrangorClient.Arrangor(
			id = overordnetArrangor.id,
			navn = overordnetArrangor.navn,
			organisasjonsnummer = overordnetArrangor.organisasjonsnummer
		)
	)

	val gjennomforingArenaData = GjennomforingArenaData(
		opprettetAar = 2022,
		lopenr = 123,
		virksomhetsnummer = arrangorMedOverordnetArrangor.organisasjonsnummer,
		ansvarligNavEnhetId = "58749854",
		status = "GJENNOMFOR",
	)

	val gjennomforingMessage = GjennomforingMessage().copy(virksomhetsnummer = arrangorMedOverordnetArrangor.organisasjonsnummer)
	val jsonObjekt = KafkaMessageCreator.opprettGjennomforingMessage(gjennomforingMessage)


	@Test
	internal fun `skal inserte gjennomforing`() {
		mockMulighetsrommetApiServer.gjennomforingArenaData(gjennomforingMessage.id, gjennomforingArenaData)
		mockArrangorServer.addArrangorResponse(arrangorMedOverordnetArrangor)
		mockArrangorServer.addArrangorResponse(overordnetArrangor)
		mockNorgHttpServer.addNavEnhet(gjennomforingArenaData.ansvarligNavEnhetId, navEnhetNavn)

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
				val maybeGjennomforing = gjennomforingRepository.get(gjennomforingMessage.id)
				maybeGjennomforing shouldNotBe null

				val gjennomforing = maybeGjennomforing!!

				gjennomforing.id shouldBe gjennomforingMessage.id
				gjennomforing.navn shouldBe gjennomforingMessage.navn
				gjennomforing.startDato shouldBe gjennomforingMessage.startDato
				gjennomforing.sluttDato shouldBe gjennomforingMessage.sluttDato
				gjennomforing.tiltakId shouldBe gjennomforingMessage.tiltakId

				gjennomforing.opprettetAar shouldBe gjennomforingArenaData.opprettetAar
				gjennomforing.lopenr shouldBe gjennomforingArenaData.lopenr
				gjennomforing.status shouldBe Gjennomforing.Status.GJENNOMFORES

				val tiltak = tiltakRepository.getAll().find { it.id == gjennomforingMessage.tiltakId }!!
				tiltak.navn shouldBe gjennomforingMessage.tiltakNavn
				tiltak.type shouldBe gjennomforingMessage.tiltakArenaKode

				val arrangor = arrangorService.getArrangorById(gjennomforing.arrangorId)
				arrangor.organisasjonsnummer shouldBe arrangorMedOverordnetArrangor.organisasjonsnummer
				arrangor.navn shouldBe arrangorMedOverordnetArrangor.navn
				arrangor.overordnetEnhetNavn shouldBe arrangorMedOverordnetArrangor.overordnetArrangor?.navn
				arrangor.overordnetEnhetOrganisasjonsnummer shouldBe arrangorMedOverordnetArrangor.overordnetArrangor?.organisasjonsnummer

				val navEnhet = navEnhetService.getNavEnhet(gjennomforing.navEnhetId!!)
				navEnhet.enhetId shouldBe gjennomforingArenaData.ansvarligNavEnhetId
				navEnhet.navn shouldBe navEnhetNavn
			}
		 }
	@Test
	internal fun `skal ikke inserte gjennomforing uten virksomhetsnummer`() {

		mockMulighetsrommetApiServer.gjennomforingArenaData(gjennomforingMessage.id, gjennomforingArenaData.copy(virksomhetsnummer = null))

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
			mockMulighetsrommetApiServer.requestCount() shouldBe 1
			gjennomforingRepository.get(gjennomforingMessage.id) shouldBe null
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
		val gjennomforingMessageIkkeStottet = gjennomforingMessage.copy(
			tiltakId = UUID.randomUUID(),
			tiltakNavn = "Individuell jobbstotte 1",
			tiltakArenaKode = "INDJOBSTOT",
			navn = "Individuell jobbstotte 1",
		)

		LogUtils.withLogs { getLogs ->
			kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(KafkaMessageCreator.opprettGjennomforingMessage(gjennomforingMessageIkkeStottet))

			AsyncUtils.eventually(until = Duration.ofSeconds(20)) {
				getLogs().any { it.message == "Lagrer ikke gjennomføring med id ${gjennomforingMessageIkkeStottet.id} og tiltakstype INDJOBSTOT fordi tiltaket ikke er støttet." } shouldBe true
			}
		}

	}
}
