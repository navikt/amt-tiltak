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
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorCommand
import no.nav.amt.tiltak.test.database.data.commands.InsertGjennomforingCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

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
			testDataRepository.insertArrangor(AKTIV_ARRANGOR_UTEN_BRUKERE)
			testDataRepository.insertGjennomforing(AKTIV_GJENNOMFORING_UTEN_BRUKERE)
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
		repository.antallArrangorer() shouldBe 4
	}

	test("antallArrangorerMedBrukere - returnerer 2") {
		repository.antallArrangorerMedBrukere() shouldBe 2
	}


	test("antallGjennomforinger - returnerer 2") {
		repository.antallGjennomforinger() shouldBe 3
	}

	test("antallGjennomforingerPrStatus - returnerer rett fordeling") {
		repository.antallGjennomforingerPrStatus() shouldHaveSize 2

		repository.antallGjennomforingerPrStatus() shouldContain
			StatusStatistikk(Gjennomforing.Status.GJENNOMFORES.name, 2)

		repository.antallGjennomforingerPrStatus() shouldContain
			StatusStatistikk(Gjennomforing.Status.AVSLUTTET.name, 1)
	}

	test("antallAktiveArrangorer - returnerer 3") {
		repository.antallAktiveArrangorer() shouldBe 2
	}

	test("antallAktiveArrangorerMedBrukere - returnerer 3") {
		repository.antallAktiveArrangorerMedBrukere() shouldBe 1
	}

	test("eksponerteBrukere - returnerer 3") {
		repository.eksponerteBrukere() shouldBe 3
	}
})



private val AKTIV_ARRANGOR_UTEN_BRUKERE = InsertArrangorCommand(
	id = UUID.fromString("d8949bb0-2fc1-47f0-a198-2ffbb1c572d7"),
	overordnet_enhet_organisasjonsnummer = "944444444",
	overordnet_enhet_navn = "Org Tiltaksarrangør 3",
	organisasjonsnummer = "444444444",
	navn = "Tiltaksarrangør uten brukere"
)

private val AKTIV_GJENNOMFORING_UTEN_BRUKERE = InsertGjennomforingCommand(
	id = UUID.fromString("c1de261e-deb1-4894-8984-cdb3d3c19740"),
	tiltak_id = TestData.TILTAK_1.id,
	arrangor_id = AKTIV_ARRANGOR_UTEN_BRUKERE.id,
	navn = "Tiltaksgjennomforing1",
	status = "GJENNOMFORES",
	start_dato = LocalDate.of(2022, 2, 1),
	slutt_dato = LocalDate.of(2050, 12, 30),
	nav_enhet_id = TestData.NAV_ENHET_1.id,
	registrert_dato = LocalDate.of(2022, 1, 1),
	fremmote_dato = LocalDate.of(2022, 2, 1),
	opprettet_aar = 2020,
	lopenr = 123
)
