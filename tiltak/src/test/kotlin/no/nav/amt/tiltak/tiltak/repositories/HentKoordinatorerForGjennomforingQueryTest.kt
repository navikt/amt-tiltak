package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertTiltaksansvarligGjennomforingTilgangCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*


class HentKoordinatorerForGjennomforingQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

	lateinit var query: HentKoordinatorerForGjennomforingQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentKoordinatorerForGjennomforingQuery(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Returnerer tomt array om gjennomføring ikke eksisterer") {
		val koordinatorer = query.query(UUID.randomUUID())
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer tomt array om gjennomføring ikke har koordinator") {
		val koordinatorer = query.query(GJENNOMFORING_2.id)
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer koordinator om gjennomføringen har en koordinator som er gyldig") {
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			InsertTiltaksansvarligGjennomforingTilgangCommand(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
		koordinatorer[0] shouldBe NAV_ANSATT_1.navn
	}

	test("Returnerer tomt om gjennomføringen ikke har noen gyldige koordinatoerer") {
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			InsertTiltaksansvarligGjennomforingTilgangCommand(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().minusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer alle koordinatoerer for en gjennomføring") {
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			InsertTiltaksansvarligGjennomforingTilgangCommand(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			InsertTiltaksansvarligGjennomforingTilgangCommand(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_2.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 2
		koordinatorer shouldContainAll listOf(NAV_ANSATT_1.navn, NAV_ANSATT_2.navn)

	}


})
