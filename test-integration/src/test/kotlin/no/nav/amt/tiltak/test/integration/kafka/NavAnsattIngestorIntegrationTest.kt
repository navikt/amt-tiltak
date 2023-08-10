package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.inputs.NavAnsattInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.NavAnsattMsg
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.AfterClass
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class NavAnsattIngestorIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var navAnsattService: NavAnsattService
	companion object {
		@JvmStatic
		@AfterClass
		fun tearDown() {
			DbTestDataUtils.cleanDatabase(SingletonPostgresContainer.getDataSource())
		}
	}

	@Test
	fun `ingest - ny nav ansatt - oppretter ny ansatt`() {
		val ansatt = mockAnsatt()
		val value = toJsonString(NavAnsattMsg(
			id = ansatt.id,
			navident = ansatt.navIdent,
			navn = ansatt.navn,
			telefon = ansatt.telefonnummer,
			epost = ansatt.epost,
		))

		kafkaMessageSender.sendTilNavAnsattTopic(ansatt.id, value)

		AsyncUtils.eventually {
			val faktiskAnsatt = navAnsattService.getNavAnsatt(ansatt.id)
			sammenlign(faktiskAnsatt, ansatt)
		}
	}

	@Test
	fun `ingest - nav ansatt finnes fra før - oppdaterer ansatt`() {
		val ansatt = mockAnsatt()
		testDataRepository.insertNavAnsatt(ansatt)

		val oppdatertAnsatt = ansatt.copy(navn = "nytt navn")

		val value = toJsonString(NavAnsattMsg(
			id = oppdatertAnsatt.id,
			navident = oppdatertAnsatt.navIdent,
			navn = oppdatertAnsatt.navn,
			telefon = oppdatertAnsatt.telefonnummer,
			epost = oppdatertAnsatt.epost,
		))

		kafkaMessageSender.sendTilNavAnsattTopic(ansatt.id, value)

		AsyncUtils.eventually {
			val faktiskAnsatt = navAnsattService.getNavAnsatt(ansatt.id)
			sammenlign(faktiskAnsatt, oppdatertAnsatt)
		}
	}

	private fun mockAnsatt() = NavAnsattInput(
		id = UUID.randomUUID(),
		navIdent = (1000 .. 9000).random().toString(),
		navn = "Veileder Veiledersen",
		epost = "veil@nav.no",
		telefonnummer = "2500052",
	)

	private fun sammenlign(faktisk: NavAnsatt, forventet: NavAnsattInput) {
		faktisk.id shouldBe forventet.id
		faktisk.navIdent shouldBe forventet.navIdent
		faktisk.navn shouldBe forventet.navn
		faktisk.telefonnummer shouldBe forventet.telefonnummer
		faktisk.epost shouldBe forventet.epost
	}


}
