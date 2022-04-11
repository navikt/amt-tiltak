package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattGjennomforingTilgang
import no.nav.amt.tiltak.test.database.data.commands.InsertTilgangForesporselCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class TilgangForesporselRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TilgangForesporselRepository

	lateinit var testDataRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TilgangForesporselRepository(NamedParameterJdbcTemplate(dataSource))

		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Skal opprette og hente foresporsel") {
		val foresporselId = UUID.randomUUID()

		repository.opprettForesporsel(OpprettForesporselInput(
			id = foresporselId,
			personligIdent = "1234",
			fornavn = "Test",
			mellomnavn = "T",
			etternavn = "Testersen",
			gjennomforingId = GJENNOMFORING_1.id,
		))

		val foresporsel = repository.hentForesporsel(foresporselId)

		foresporsel.id shouldBe foresporselId
		foresporsel.personligIdent shouldBe "1234"
		foresporsel.fornavn shouldBe "Test"
		foresporsel.mellomnavn shouldBe "T"
		foresporsel.etternavn shouldBe "Testersen"
		foresporsel.gjennomforingId shouldBe GJENNOMFORING_1.id
	}

	test("hentUbesluttedeForesporsler skal hente ubesluttede forespørsler") {
		val foresporselId1 = UUID.randomUUID()
		val foresporselId2 = UUID.randomUUID()

		testDataRepository.insertTilgangForesporsel(InsertTilgangForesporselCommand(
			id = foresporselId1,
			personligIdent = "",
			fornavn = "",
			etternavn = "",
			gjennomforingId = GJENNOMFORING_1.id,
		))

		testDataRepository.insertTilgangForesporsel(InsertTilgangForesporselCommand(
			id = foresporselId2,
			personligIdent = "",
			fornavn = "",
			etternavn = "",
			gjennomforingId = GJENNOMFORING_1.id,
			beslutning = "GODKJENT",
			beslutningAvNavAnsattId = NAV_ANSATT_1.id,
			tidspunktBeslutning = ZonedDateTime.now()
		))

		val foresporsler = repository.hentUbesluttedeForesporsler(GJENNOMFORING_1.id)

		foresporsler shouldHaveSize 1

		foresporsler.first().id shouldBe foresporselId1
	}

	test("godkjennForesporsel skal oppdatere riktig verdier") {
		val foresporselId = UUID.randomUUID()

		testDataRepository.insertTilgangForesporsel(InsertTilgangForesporselCommand(
			id = foresporselId,
			personligIdent = "",
			fornavn = "",
			etternavn = "",
			gjennomforingId = GJENNOMFORING_1.id,
		))

		val tilgangId = UUID.randomUUID()

		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			InsertArrangorAnsattGjennomforingTilgang(
				id = tilgangId,
				ansatt_id = ARRANGOR_ANSATT_1.id,
				gjennomforing_id = GJENNOMFORING_1.id,
			)
		)

		repository.godkjennForesporsel(foresporselId, NAV_ANSATT_1.id, tilgangId)

		val foresporsel = repository.hentForesporsel(foresporselId)

		foresporsel.beslutning shouldBe Beslutning.GODKJENT
		foresporsel.beslutningAvNavAnsattId shouldBe NAV_ANSATT_1.id
		foresporsel.gjennomforingTilgangId shouldBe tilgangId
		foresporsel.tidspunktBeslutning!!.toLocalDate() shouldBe LocalDate.now()
	}

	test("avvisForesporsel skal oppdatere riktig verdier") {
		val foresporselId = UUID.randomUUID()

		testDataRepository.insertTilgangForesporsel(InsertTilgangForesporselCommand(
			id = foresporselId,
			personligIdent = "",
			fornavn = "",
			etternavn = "",
			gjennomforingId = GJENNOMFORING_1.id,
		))

		repository.avvisForesporsel(foresporselId, NAV_ANSATT_1.id)

		val foresporsel = repository.hentForesporsel(foresporselId)

		foresporsel.beslutning shouldBe Beslutning.AVVIST
		foresporsel.beslutningAvNavAnsattId shouldBe NAV_ANSATT_1.id
		foresporsel.tidspunktBeslutning!!.toLocalDate() shouldBe LocalDate.now()
	}

})
