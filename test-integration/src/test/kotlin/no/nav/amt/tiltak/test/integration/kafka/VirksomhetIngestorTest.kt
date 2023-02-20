package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
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
import java.util.*

class VirksomhetIngestorTest : IntegrationTestBase() {

	@Autowired
	lateinit var arrangorService: ArrangorService


	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
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
		val nyOverordnetEnhet = EnhetDto(
			organisasjonsnummer = "999123456",
			navn = "Ny Overordnet Enhet",
			overordnetEnhetNavn = "Mor Org",
			overordnetEnhetOrganisasjonsnummer = "888666555",
		)

		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.organisasjonsnummer,
			navn = ARRANGOR_1.navn,
			overordnetEnhetOrganisasjonsnummer = nyOverordnetEnhet.organisasjonsnummer,
		)

		mockEnhetsregisterServer.addEnhet(nyOverordnetEnhet)

		kafkaMessageSender.sendTilVirksomhetTopic(
			KafkaMessageCreator.opprettVirksomhetMessage(msg)
		)

		AsyncUtils.eventually {
			val oppdatertArrangor = arrangorService.getArrangorById(ARRANGOR_1.id)

			oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe nyOverordnetEnhet.organisasjonsnummer
			oppdatertArrangor.overordnetEnhetNavn shouldBe nyOverordnetEnhet.navn
		}
	}

	@Test
	fun `ingest - nytt navn virksomheten er en overordnet enhet men ikke arrangor - skal oppdatere overordnet enhet navn hos arrangor`() {
		val msg = VirksomhetMessage(
			organisasjonsnummer = ARRANGOR_1.overordnetEnhetOrganisasjonsnummer!!,
			navn = "Nytt overordnet enhet navn",
			overordnetEnhetOrganisasjonsnummer = "42",
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
