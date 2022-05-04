package no.nav.amt.tiltak.tilgangskontroll.tiltaksansvarlig_tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertTiltaksansvarligGjennomforingTilgangCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*


class TiltaksansvarligGjennomforingTilgangRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TiltaksansvarligGjennomforingTilgangRepository

	lateinit var testDataRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TiltaksansvarligGjennomforingTilgangRepository(NamedParameterJdbcTemplate(dataSource))

		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("hentAktiveTilganger - skal hente tilganger") {
		val id = UUID.randomUUID()

		repository.opprettTilgang(
			id = id,
			navAnsattId = NAV_ANSATT_1.id,
			gjennomforingId = GJENNOMFORING_1.id,
			gyldigTil = ZonedDateTime.now().plusDays(1)
		)

		repository.opprettTilgang(
			id = UUID.randomUUID(),
			navAnsattId = NAV_ANSATT_1.id,
			gjennomforingId = GJENNOMFORING_1.id,
			gyldigTil = ZonedDateTime.now().minusDays(1)
		)

		val tilganger = repository.hentAktiveTilganger(NAV_ANSATT_1.id)

		tilganger shouldHaveSize 1

		tilganger.first().id shouldBe id
	}

	test("opprettTilgang - skal opprette tilgang") {
		val id = UUID.randomUUID()
		val gyldigTil = ZonedDateTime.now().plusDays(1)

		repository.opprettTilgang(
			id = id,
			navAnsattId = NAV_ANSATT_1.id,
			gjennomforingId = GJENNOMFORING_1.id,
			gyldigTil = gyldigTil
		)

		val tilgang = repository.hentTilgang(id)

		tilgang.id shouldBe id
		tilgang.navAnsattId shouldBe NAV_ANSATT_1.id
		tilgang.gjennomforingId shouldBe GJENNOMFORING_1.id
		tilgang.gyldigTil shouldBeEqualTo gyldigTil
		tilgang.createdAt shouldBeCloseTo ZonedDateTime.now()
	}

	test("stopTilgang - skal stoppe tilgang") {
		val id = UUID.randomUUID()
		val gyldigTil = ZonedDateTime.now().plusDays(1)

		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			InsertTiltaksansvarligGjennomforingTilgangCommand(
				id = id,
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = gyldigTil,
				createdAt = ZonedDateTime.now()
			)
		)

		repository.stopTilgang(id)

		val tilgang = repository.hentTilgang(id)

		tilgang.gyldigTil shouldBeBefore ZonedDateTime.now()
	}

})
