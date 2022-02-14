package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.TestData.DELTAKER_1_ID
import no.nav.amt.tiltak.test.database.TestData.DELTAKER_2_ID
import no.nav.amt.tiltak.test.database.TestData.GJENNOMFORING_1_ID
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

class DeltakerDetaljerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var getDeltakerDetaljerQuery: GetDeltakerDetaljerQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		getDeltakerDetaljerQuery = GetDeltakerDetaljerQuery(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Should get deltaker detaljer") {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(DELTAKER_1_ID)
			?: fail("deltakerDetaljer should not be null")

		deltakerDetaljer.deltakerId shouldBe DELTAKER_1_ID
		deltakerDetaljer.fornavn shouldBe "Bruker 1 fornavn"
		deltakerDetaljer.mellomnavn shouldBe null
		deltakerDetaljer.etternavn shouldBe "Bruker 1 etternavn"
		deltakerDetaljer.fodselsnummer shouldBe "12345678910"
		deltakerDetaljer.telefonnummer shouldBe "73404782"
		deltakerDetaljer.epost shouldBe "bruker1@example.com"
		deltakerDetaljer.navKontorNavn shouldBe "NAV Testheim"
		deltakerDetaljer.veilederNavn shouldBe "Vashnir Veiledersen"
		deltakerDetaljer.veilederTelefonnummer shouldBe "88776655"
		deltakerDetaljer.veilederEpost shouldBe "vashnir.veiledersen@nav.no"
		deltakerDetaljer.startDato shouldBe LocalDate.of(2022, 2, 13)
		deltakerDetaljer.sluttDato shouldBe LocalDate.of(2030, 2, 14)
		deltakerDetaljer.status shouldBe Deltaker.Status.DELTAR
		deltakerDetaljer.gjennomforingId shouldBe GJENNOMFORING_1_ID
		deltakerDetaljer.gjennomforingStartDato shouldBe LocalDate.of(2022,2, 1)
		deltakerDetaljer.gjennomforingSluttDato shouldBe LocalDate.of(2050, 12, 30)
		deltakerDetaljer.tiltakNavn shouldBe "Tiltak1"
		deltakerDetaljer.tiltakKode shouldBe "AMO"
	}

	test("Should get deltaker detaljer if nav ansatt is null") {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(DELTAKER_2_ID)

		deltakerDetaljer shouldNotBe null
	}

})

