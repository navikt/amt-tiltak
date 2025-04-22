package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.mockNavBruker
import no.nav.amt.tiltak.test.integration.utils.DeltakerMessage
import no.nav.amt.tiltak.test.integration.utils.GjennomforingMessage
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.utils.AsyncUtils
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class ArenaDeltakerProcessorIntegrationTest : IntegrationTestBase() {

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
	@Disabled
	fun `ingest deltaker - gjennomforing er ingestet`() {
		val mockNavBruker = mockNavBruker(
			BRUKER_1.copy(
				id = UUID.randomUUID(),
				personIdent = (1..Long.MAX_VALUE).random().toString()
			),
			NAV_ENHET_1,
		)

		mockAmtPersonHttpServer.addNavBrukerResponse(mockNavBruker)

		val gjennomforing = ingestGjennomforing()
		val message = DeltakerMessage(gjennomforingId = gjennomforing.id, personIdent = mockNavBruker.personident)


		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val maybeDeltaker = deltakerService.hentDeltaker(message.id)

			maybeDeltaker shouldNotBe null

			val deltaker = maybeDeltaker!!
			deltaker.personIdent shouldBe message.personIdent
			deltaker.gjennomforingId shouldBe message.gjennomforingId
			deltaker.prosentStilling shouldBe message.prosentDeltid
			deltaker.dagerPerUke shouldBe message.dagerPerUke
			deltaker.registrertDato shouldBeEqualTo message.registrertDato
			deltaker.startDato shouldBe message.startDato
			deltaker.sluttDato shouldBe message.sluttDato
			deltaker.status.type shouldBe message.status
			deltaker.status.aarsak shouldBe message.statusAarsak
			deltaker.innsokBegrunnelse shouldBe message.innsokBegrunnelse
			deltaker.erSkjermet shouldBe false
			deltaker.fornavn shouldBe mockNavBruker.fornavn
			deltaker.etternavn shouldBe mockNavBruker.etternavn
			deltaker.kilde shouldBe Kilde.ARENA
		}

	}

	@Test
	@Disabled
	fun `ingest deltaker - gjennomforing er kurs - ingestes uten feil`() {
		val mockNavBruker = mockNavBruker(
			BRUKER_1.copy(
				id = UUID.randomUUID(),
				personIdent = (1..Long.MAX_VALUE).random().toString()
			),
			NAV_ENHET_1,
		)

		mockAmtPersonHttpServer.addNavBrukerResponse(mockNavBruker)

		val gjennomforing = ingestGjennomforing(tiltakKode = "GRUPPEAMO")
		val message = DeltakerMessage(
			gjennomforingId = gjennomforing.id,
			personIdent = mockNavBruker.personident,
			status = DeltakerStatus.Type.VURDERES
		)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val maybeDeltaker = deltakerService.hentDeltaker(message.id)

			maybeDeltaker shouldNotBe null

			val deltaker = maybeDeltaker!!
			deltaker.personIdent shouldBe message.personIdent
			deltaker.gjennomforingId shouldBe message.gjennomforingId
			deltaker.prosentStilling shouldBe message.prosentDeltid
			deltaker.dagerPerUke shouldBe message.dagerPerUke
			deltaker.registrertDato shouldBeEqualTo message.registrertDato
			deltaker.startDato shouldBe message.startDato
			deltaker.sluttDato shouldBe message.sluttDato
			deltaker.status.type shouldBe message.status
			deltaker.status.aarsak shouldBe message.statusAarsak
			deltaker.innsokBegrunnelse shouldBe message.innsokBegrunnelse
			deltaker.erSkjermet shouldBe false
			deltaker.fornavn shouldBe mockNavBruker.fornavn
			deltaker.etternavn shouldBe mockNavBruker.etternavn
			deltaker.kilde shouldBe Kilde.ARENA
		}

	}

	@Test
	fun `ingest deltaker - deltaker er skjermet - skal inserte ny deltaker med skjermingsdata`() {
		val mockNavBruker = mockNavBruker(
			BRUKER_1.copy(
				id = UUID.randomUUID(),
				personIdent = (1..Long.MAX_VALUE).random().toString(),
				erSkjermet = true,
			),
			NAV_ENHET_1,
		)

		mockAmtPersonHttpServer.addNavBrukerResponse(mockNavBruker)


		val message = DeltakerMessage(gjennomforingId = GJENNOMFORING_1.id, personIdent = mockNavBruker.personident)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val maybeDeltaker = deltakerService.hentDeltaker(message.id)

			maybeDeltaker shouldNotBe null

			val deltaker = maybeDeltaker!!
			deltaker.personIdent shouldBe message.personIdent
			deltaker.gjennomforingId shouldBe message.gjennomforingId
			deltaker.erSkjermet shouldBe true
			deltaker.fornavn shouldBe mockNavBruker.fornavn
			deltaker.etternavn shouldBe mockNavBruker.etternavn
		}

	}

	@Test
	@Disabled
	fun `ingest deltaker - status feilregistrert - skal oppdatere deltaker`() {
		val mockNavBruker = mockNavBruker(
			BRUKER_1.copy(
				id = UUID.randomUUID(),
				personIdent = (1..Long.MAX_VALUE).random().toString()
			),
			NAV_ENHET_1,
		)

		mockAmtPersonHttpServer.addNavBrukerResponse(mockNavBruker)

		val gjennomforing = ingestGjennomforing()
		val message = DeltakerMessage(
			gjennomforingId = gjennomforing.id,
			personIdent = mockNavBruker.personident,
			status = DeltakerStatus.Type.FEILREGISTRERT
		)

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			val deltaker = deltakerService.hentDeltaker(message.id)
			deltaker shouldNotBe null
			deltaker?.status?.type shouldBe DeltakerStatus.Type.FEILREGISTRERT
		}

	}

	@Test
	fun `ingest deltaker - DELETE operation - skal slette deltaker`() {
		deltakerService.hentDeltaker(DELTAKER_1.id) shouldNotBe null

		val message =
			DeltakerMessage(id = DELTAKER_1.id, gjennomforingId = DELTAKER_1.gjennomforingId, operation = "DELETED")

		kafkaMessageSender.sendTilAmtTiltakTopic(KafkaMessageCreator.opprettAmtTiltakDeltakerMessage(message))

		AsyncUtils.eventually {
			deltakerService.hentDeltaker(DELTAKER_1.id) shouldBe null
		}
	}

	private fun ingestGjennomforing(tiltakKode: String = "GRUPPEAMO"): GjennomforingMessage {
		val overordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
			id = UUID.randomUUID(),
			organisasjonsnummer = "888666555",
			navn = "Arrangor Org",
			overordnetArrangor = null
		)
		val arrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
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
			lopenr = 123
		)

		val gjennomforingMessage =
			GjennomforingMessage(tiltakArenaKode = tiltakKode, virksomhetsnummer = arrangor.organisasjonsnummer)
		val jsonObjekt = KafkaMessageCreator.opprettGjennomforingMessage(gjennomforingMessage)

		mockMulighetsrommetApiServer.gjennomforingArenaData(gjennomforingMessage.id, gjennomforingArenaData)

		mockArrangorServer.addArrangorResponse(arrangor)
		mockArrangorServer.addArrangorResponse(overordnetArrangor)

		kafkaMessageSender.sendTilSisteTiltaksgjennomforingTopic(jsonObjekt)

		AsyncUtils.eventually {
			val maybeGjennomforing = gjennomforingRepository.get(gjennomforingMessage.id)
			maybeGjennomforing shouldNotBe null
		}
		return gjennomforingMessage
	}
}
