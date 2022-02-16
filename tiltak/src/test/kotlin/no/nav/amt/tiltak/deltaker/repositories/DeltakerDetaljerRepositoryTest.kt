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
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_KONTOR_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

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
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(DELTAKER_1.id)
			?: fail("deltakerDetaljer should not be null")

		deltakerDetaljer.deltakerId shouldBe DELTAKER_1.id
		deltakerDetaljer.fornavn shouldBe BRUKER_1.fornavn
		deltakerDetaljer.mellomnavn shouldBe null
		deltakerDetaljer.etternavn shouldBe BRUKER_1.etternavn
		deltakerDetaljer.fodselsnummer shouldBe BRUKER_1.fodselsnummer
		deltakerDetaljer.telefonnummer shouldBe BRUKER_1.telefonnummer
		deltakerDetaljer.epost shouldBe BRUKER_1.epost
		deltakerDetaljer.navKontorNavn shouldBe NAV_KONTOR_1.navn
		deltakerDetaljer.veilederNavn shouldBe NAV_ANSATT_1.navn
		deltakerDetaljer.veilederTelefonnummer shouldBe NAV_ANSATT_1.telefonnummer
		deltakerDetaljer.veilederEpost shouldBe NAV_ANSATT_1.epost
		deltakerDetaljer.startDato shouldBe DELTAKER_1.start_dato
		deltakerDetaljer.sluttDato shouldBe DELTAKER_1.slutt_dato
		deltakerDetaljer.status shouldBe Deltaker.Status.DELTAR
		deltakerDetaljer.gjennomforingId shouldBe GJENNOMFORING_1.id
		deltakerDetaljer.gjennomforingStartDato shouldBe GJENNOMFORING_1.start_dato
		deltakerDetaljer.gjennomforingSluttDato shouldBe GJENNOMFORING_1.slutt_dato
		deltakerDetaljer.tiltakNavn shouldBe TILTAK_1.navn
		deltakerDetaljer.tiltakKode shouldBe TILTAK_1.type
	}

	test("Should get deltaker detaljer if nav ansatt is null") {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(DELTAKER_2.id)

		deltakerDetaljer shouldNotBe null
	}

})

