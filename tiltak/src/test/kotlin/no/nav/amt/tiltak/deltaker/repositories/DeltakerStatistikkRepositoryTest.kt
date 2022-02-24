package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.DELTAR
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.VENTER_PA_OPPSTART
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class DeltakerStatistikkRepositoryTest : FunSpec({

		val dataSource = SingletonPostgresContainer.getDataSource()

		lateinit var repository: DeltakerStatistikkRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerStatistikkRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource) { testDataRepository ->
			TestDataSeeder.insertDefaultTestData(testDataRepository)

			testDataRepository.insertArrangor(TestData.ARRANGOR_3)
			testDataRepository.insertBruker(TestData.BRUKER_4)
			testDataRepository.insertDeltaker(TestData.DELTAKER_4)
			testDataRepository.insertDeltakerStatus(TestData.DELTAKER_4_STATUS_1)
		}
	}


	test("antallDeltakere - returnerer 3") {
		repository.antallDeltakere() shouldBe 3
	}

	test("antallDeltakerePerStatus - returnerer forventet antall og fordeling") {
		repository.antallDeltakerePerStatus() shouldHaveSize 2
		repository.antallDeltakerePerStatus() shouldContain StatusStatistikk(DELTAR.name, 2)
		repository.antallDeltakerePerStatus() shouldContain StatusStatistikk(VENTER_PA_OPPSTART.name, 1)
	}


	test("antallArrangorer - returnerer 3") {
		repository.antallArrangorer() shouldBe 3
	}

	test("antallArrangorerMedBrukere - returnerer 2") {
		repository.antallArrangorerMedBrukere() shouldBe 2
	}


	test("antallGjennomforinger - returnerer 2") {
		repository.antallGjennomforinger() shouldBe 2
	}

	test("antallGjennomforingerPrStatus - returnerer rett fordeling") {
		repository.antallGjennomforingerPrStatus() shouldHaveSize 2

		repository.antallGjennomforingerPrStatus() shouldContain
			StatusStatistikk(Gjennomforing.Status.GJENNOMFORES.name, 1)

		repository.antallGjennomforingerPrStatus() shouldContain
			StatusStatistikk(Gjennomforing.Status.AVSLUTTET.name, 1)
	}

	test("eksponerteBrukere - returnerer 3") {
		repository.eksponerteBrukere() shouldBe 3
	}
})
