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
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.util.*

class DeltakerStatistikkRepositoryTest : FunSpec({

		val dataSource = SingletonPostgresContainer.getDataSource()

		lateinit var repository: DeltakerStatistikkRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerStatistikkRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/deltaker-statistikk-repository_test-data.sql")
	}


	test("antallDeltakere - returnerer 3") {
		repository.antallDeltakere() shouldBe 3
	}

	test("antallDeltakerePerStatus - returnerer forventet antall og fordeling") {
		repository.antallDeltakerePerStatus() shouldHaveSize 2
		repository.antallDeltakerePerStatus() shouldContain StatusStatistikk(DELTAR.name, 2)
		repository.antallDeltakerePerStatus() shouldContain StatusStatistikk(VENTER_PA_OPPSTART.name, 1)
	}


	test("antallArrangorer - returnerer 2") {
		repository.antallArrangorer() shouldBe 2
	}

	test("antallArrangorerMedBrukere - returnerer 1") {
		repository.antallArrangorerMedBrukere() shouldBe 1
	}


	test("antallGjennomforinger - returnerer 1") {
		repository.antallGjennomforinger() shouldBe 1
	}

	test("antallGjennomforingerPrStatus - returnerer rett fordeling") {
		repository.antallGjennomforingerPrStatus() shouldHaveSize 1
		repository.antallGjennomforingerPrStatus() shouldContain
			StatusStatistikk(Gjennomforing.Status.GJENNOMFORES.name, 1)
	}
})
