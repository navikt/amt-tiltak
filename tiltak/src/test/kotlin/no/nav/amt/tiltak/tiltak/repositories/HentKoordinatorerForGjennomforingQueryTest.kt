package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattCommand
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattGjennomforingTilgang
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattRolleCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*


class HentKoordinatorerForGjennomforingQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

	lateinit var query: HentKoordinatorerForGjennomforingQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentKoordinatorerForGjennomforingQuery(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanDatabase(dataSource)

		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1)

	}

	fun createAnsatt(
		baseCommand: InsertArrangorAnsattCommand = ARRANGOR_ANSATT_1,
		fornavn: String = "Steve",
		mellomnavn: String? = null,
		etternavn: String = "Mc Guffin"
	) {
		testDataRepository.insertArrangorAnsatt(
			baseCommand.copy(
				fornavn = fornavn,
				mellomnavn = mellomnavn,
				etternavn = etternavn
			)
		)
	}

	fun createRolleForAnsatt(
		rolleName: String,
		ansattId: UUID = ARRANGOR_ANSATT_1.id
	) {
		testDataRepository.insertArrangorAnsattRolle(
			InsertArrangorAnsattRolleCommand(
				id = UUID.randomUUID(),
				arrangorId = ARRANGOR_1.id,
				ansattId = ansattId,
				rolle = rolleName
			)
		)
	}

	fun createGjennomforingTilgang(ansattId: UUID = ARRANGOR_ANSATT_1.id) {
		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			InsertArrangorAnsattGjennomforingTilgang(
				id = UUID.randomUUID(),
				ansattId = ansattId,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)
	}


	test("Returnerer tomt array om gjennomføring ikke eksisterer") {
		val koordinatorer = query.query(UUID.randomUUID())
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer tomt array om gjennomføring ikke har koordinator") {
		val koordinatorer = query.query(GJENNOMFORING_2.id)
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer ikke personer som ikke er koordinatorer") {
		createAnsatt()
		createRolleForAnsatt("VEILEDER")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer shouldBe emptyList()
	}

	test("Returnerer koordinator om gjennomføringen har en koordinator") {
		createAnsatt()
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
		koordinatorer[0] shouldBe "Steve Mc Guffin"
	}

	test("Returnerer koordinator med mellomnavn om gjennomføringen har en koordinator") {
		createAnsatt(mellomnavn = "mellomnavn")
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
		koordinatorer[0] shouldBe "Steve mellomnavn Mc Guffin"
	}

	test("Returnerer alle koordinatoerer for en gjennomføring") {
		createAnsatt()
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		createAnsatt(
			baseCommand = ARRANGOR_ANSATT_2,
			fornavn = "Jane",
			etternavn = "Doe"
		)

		createRolleForAnsatt("KOORDINATOR", ARRANGOR_ANSATT_2.id)
		createGjennomforingTilgang(ARRANGOR_ANSATT_2.id)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 2
		koordinatorer shouldContainAll listOf(
			"Jane Doe",
			"Steve Mc Guffin"
		)

	}


})
