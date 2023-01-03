package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.AsyncUtils
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
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


	val id = UUID.randomUUID()
	val navn = "Oppfølging Gjennomføring"
	val startDato = "2022-12-19"
	val sluttDato = "2023-12-19"

	val tiltakId = UUID.randomUUID()
	val tiltaksNavn = "Tiltaks navn"
	val arenaKode = "INDOPFAG"

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

	val jsonObjekt = """
			{
				"id": "$id",
				"tiltakstype": {
					"id": "$tiltakId",
					"navn": "$tiltaksNavn",
					"arenaKode": "$arenaKode"
				},
				"navn": "$navn",
				"startDato": "$startDato",
				"sluttDato": "$sluttDato"
			}
		""".trimIndent()


	@Test
	internal fun `skal inserte gjennomforing`() {

		mockMulighetsrommetApiServer.gjennomforingArenaData(id, gjennomforingArenaData)

		mockEnhetsregisterServer.addEnhet(EnhetDto(
			organisasjonsnummer = virksomhetsnummer,
			navn = arrangorNavn,
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrgNr,
		))

		mockNorgHttpServer.addNavEnhet(navEnhetId, navEnhetNavn)

		kafkaMessageSender.sendTilSisteTitaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
				val maybeGjennomforing = gjennomforingRepository.get(id)
				maybeGjennomforing shouldNotBe null

				val gjennomforing = maybeGjennomforing!!

				gjennomforing.id shouldBe id
				gjennomforing.navn shouldBe navn
				gjennomforing.startDato shouldBe LocalDate.parse(startDato)
				gjennomforing.sluttDato shouldBe LocalDate.parse(sluttDato)
				gjennomforing.deprecated shouldBe false
				gjennomforing.tiltakId shouldBe tiltakId

				gjennomforing.opprettetAar shouldBe gjennomforingArenaData.opprettetAar
				gjennomforing.lopenr shouldBe gjennomforingArenaData.lopenr
				gjennomforing.status shouldBe Gjennomforing.Status.GJENNOMFORES

				val tiltak = tiltakRepository.getAll().find { it.id == tiltakId }!!
				tiltak.navn shouldBe tiltaksNavn
				tiltak.type shouldBe arenaKode

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

		mockMulighetsrommetApiServer.gjennomforingArenaData(id, gjennomforingArenaData.copy(virksomhetsnummer = null))

		kafkaMessageSender.sendTilSisteTitaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
			mockMulighetsrommetApiServer.requestCount() shouldBe 1
			gjennomforingRepository.get(id) shouldBe null
		}

	}
}
