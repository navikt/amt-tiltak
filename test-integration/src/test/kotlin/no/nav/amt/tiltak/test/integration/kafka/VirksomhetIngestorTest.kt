package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageCreator
import no.nav.amt.tiltak.test.integration.utils.VirksomhetMessage
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class VirksomhetIngestorTest : IntegrationTestBase() {

	@Autowired
	lateinit var arrangorService: ArrangorService

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		val overordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
			id = UUID.randomUUID(),
			navn = ARRANGOR_1.overordnetEnhetNavn!!,
			organisasjonsnummer = ARRANGOR_1.overordnetEnhetOrganisasjonsnummer!!,
			overordnetArrangorId = null,
			overordnetArrangorNavn = null,
			overordnetArrangorOrgnummer = null,
			deltakerlister = emptySet()
		)
		mockArrangorServer.addArrangorResponse(overordnetArrangor)
		mockArrangorServer.addArrangorResponse(AmtArrangorClient.ArrangorMedOverordnetArrangor(
			id = ARRANGOR_1.id,
			navn = ARRANGOR_1.navn,
			organisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			overordnetArrangorId = overordnetArrangor.id,
			overordnetArrangorNavn = overordnetArrangor.overordnetArrangorNavn,
			overordnetArrangorOrgnummer = overordnetArrangor.overordnetArrangorOrgnummer,
			deltakerlister = emptySet()
		))
	}

	@Test
	fun `ingest - nytt navn - skal oppdatere arrangor og underenheter`() {

		val underenhet = ArrangorInput(
			id = UUID.randomUUID(),
			navn = "Underenhet 1",
			organisasjonsnummer = "777888999",
			overordnetEnhetOrganisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			overordnetEnhetNavn = ARRANGOR_1.navn
		)
		testDataRepository.insertArrangor(underenhet)

		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			navn = "Nytt Virksomhetsnavn",
			overordnetEnhetOrganisasjonsnummer = ARRANGOR_1.overordnetEnhetOrganisasjonsnummer,
		)

		kafkaMessageSender.sendTilVirksomhetTopic(
			KafkaMessageCreator.opprettVirksomhetMessage(msg)
		)

		AsyncUtils.eventually {
			val oppdatertArrangor = arrangorService.getArrangorById(ARRANGOR_1.id)
			oppdatertArrangor.navn shouldBe msg.navn
			oppdatertArrangor.organisasjonsnummer shouldBe msg.organisasjonsnummer
			oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe msg.overordnetEnhetOrganisasjonsnummer

			val oppdatertUnderenhet = arrangorService.getArrangorById(underenhet.id)
			oppdatertUnderenhet.overordnetEnhetNavn shouldBe oppdatertArrangor.navn
		}
	}

	@Test
	fun `ingest - overordnetEnhetOrgnr er null - skal oppdatere arrangor`() {
		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			navn = "Nytt Virksomhetsnavn",
			overordnetEnhetOrganisasjonsnummer = null,
		)

		kafkaMessageSender.sendTilVirksomhetTopic(
			KafkaMessageCreator.opprettVirksomhetMessage(msg)
		)

		AsyncUtils.eventually {
			val oppdatertArrangor = arrangorService.getArrangorById(ARRANGOR_1.id)
			oppdatertArrangor.navn shouldBe msg.navn
			oppdatertArrangor.organisasjonsnummer shouldBe msg.organisasjonsnummer
			oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe msg.overordnetEnhetOrganisasjonsnummer
			oppdatertArrangor.overordnetEnhetNavn shouldBe null
		}
	}

	@Test
	fun `ingest - nytt overordnet enhet orgnr - skal oppdatere overordnet enhet navn og orgnr`() {
		val nyOverordnetArrangor = AmtArrangorClient.ArrangorMedOverordnetArrangor(
			id = UUID.randomUUID(),
			navn = "Ny Overordnet Enhet",
			organisasjonsnummer = "999123456",
			overordnetArrangorId = UUID.randomUUID(),
			overordnetArrangorNavn = "Mor Org",
			overordnetArrangorOrgnummer = "888666555",
			deltakerlister = emptySet()
		)

		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			navn = ARRANGOR_1.navn,
			overordnetEnhetOrganisasjonsnummer = nyOverordnetArrangor.organisasjonsnummer,
		)

		mockArrangorServer.addArrangorResponse(nyOverordnetArrangor)

		kafkaMessageSender.sendTilVirksomhetTopic(
			KafkaMessageCreator.opprettVirksomhetMessage(msg)
		)

		AsyncUtils.eventually {
			val oppdatertArrangor = arrangorService.getArrangorById(ARRANGOR_1.id)

			oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe nyOverordnetArrangor.organisasjonsnummer
			oppdatertArrangor.overordnetEnhetNavn shouldBe nyOverordnetArrangor.navn
		}
	}

	@Test
	fun `ingest - nytt navn virksomheten er en overordnet enhet men ikke arrangor - skal oppdatere overordnet enhet navn hos arrangor`() {
		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.overordnetEnhetOrganisasjonsnummer!!,
			navn = "Nytt overordnet enhet navn",
			overordnetEnhetOrganisasjonsnummer = "42",
		)

		mockArrangorServer.addArrangorResponse(
			AmtArrangorClient.ArrangorMedOverordnetArrangor(
				id = ARRANGOR_1.id,
				organisasjonsnummer = msg.organisasjonsnummer,
				navn = msg.navn,
				overordnetArrangorId = UUID.randomUUID(),
				overordnetArrangorOrgnummer = msg.overordnetEnhetOrganisasjonsnummer,
				overordnetArrangorNavn = "",
				deltakerlister = emptySet()
			)
		)

		kafkaMessageSender.sendTilVirksomhetTopic(
			KafkaMessageCreator.opprettVirksomhetMessage(msg)
		)

		AsyncUtils.eventually {
			val oppdatertArrangor = arrangorService.getArrangorById(ARRANGOR_1.id)

			oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe msg.organisasjonsnummer
			oppdatertArrangor.overordnetEnhetNavn shouldBe msg.navn
		}
	}
}
