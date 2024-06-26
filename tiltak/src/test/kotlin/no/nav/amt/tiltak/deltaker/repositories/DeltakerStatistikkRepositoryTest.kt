package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus.Type.DELTAR
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus.Type.VENTER_PA_OPPSTART
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorInput
import no.nav.amt.tiltak.test.database.data.inputs.GjennomforingInput
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

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

	test("antallAktiveArrangorer - returnerer 3") {
		repository.antallAktiveArrangorer() shouldBe 2
	}

	test("antallAktiveArrangorerMedBrukere - returnerer 3") {
		repository.antallAktiveArrangorerMedBrukere() shouldBe 1
	}

	test("eksponerteBrukerePerStatus - returnerer 3") {
		repository.eksponerteBrukerePrStatus() shouldHaveSize 2
		repository.eksponerteBrukerePrStatus() shouldContain StatusStatistikk("DELTAR", 2)
		repository.eksponerteBrukerePrStatus() shouldContain StatusStatistikk("VENTER_PA_OPPSTART", 1)
	}
})



private val AKTIV_ARRANGOR_UTEN_BRUKERE = ArrangorInput(
	id = UUID.fromString("d8949bb0-2fc1-47f0-a198-2ffbb1c572d7"),
	overordnetEnhetOrganisasjonsnummer = "944444444",
	overordnetEnhetNavn = "Org Tiltaksarrangør 3",
	organisasjonsnummer = "444444444",
	navn = "Tiltaksarrangør uten brukere"
)

private val AKTIV_GJENNOMFORING_UTEN_BRUKERE = GjennomforingInput(
	id = UUID.fromString("c1de261e-deb1-4894-8984-cdb3d3c19740"),
	tiltakId = TestData.TILTAK_1.id,
	arrangorId = AKTIV_ARRANGOR_UTEN_BRUKERE.id,
	navn = "Tiltaksgjennomforing1",
	status = "GJENNOMFORES",
	startDato = LocalDate.of(2022, 2, 1),
	sluttDato = LocalDate.of(2050, 12, 30),
	createdAt = LocalDateTime.now(),
	modifiedAt = LocalDateTime.now(),
	opprettetAar = 2020,
	lopenr = 123,
	erKurs = false
)
