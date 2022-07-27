package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.port.Person
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattRolleInput
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
		baseInput: ArrangorAnsattInput = ARRANGOR_ANSATT_1,
		fornavn: String = "Steve",
		mellomnavn: String? = null,
		etternavn: String = "Mc Guffin"
	) {
		testDataRepository.insertArrangorAnsatt(
			baseInput.copy(
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
			ArrangorAnsattRolleInput(
				id = UUID.randomUUID(),
				arrangorId = ARRANGOR_1.id,
				ansattId = ansattId,
				rolle = rolleName
			)
		)
	}

	fun createGjennomforingTilgang(ansattId: UUID = ARRANGOR_ANSATT_1.id) {
		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = UUID.randomUUID(),
				ansattId = ansattId,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)
	}

	fun person(fornavn: String, mellomnavn: String, etternavn: String): Person {
		return Person(
			fornavn = fornavn,
			mellomnavn = mellomnavn,
			etternavn = etternavn,
			telefonnummer = null,
			diskresjonskode = null
		)
	}

	fun person(fornavn: String, etternavn: String): Person {
		return Person(
			fornavn = fornavn,
			mellomnavn = null,
			etternavn = etternavn,
			telefonnummer = null,
			diskresjonskode = null
		)

	}

	test("Returnerer tomt array om gjennomføring ikke eksisterer") {
		val koordinatorer = query.query(UUID.randomUUID())
		koordinatorer shouldBe emptySet()
	}

	test("Returnerer tomt array om gjennomføring ikke har koordinator") {
		val koordinatorer = query.query(GJENNOMFORING_2.id)
		koordinatorer shouldBe emptySet()
	}

	test("Returnerer ikke personer som ikke er koordinatorer") {
		createAnsatt()
		createRolleForAnsatt("VEILEDER")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer shouldBe emptySet()
	}

	test("Returnerer koordinator om gjennomføringen har en koordinator") {
		createAnsatt()
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
		koordinatorer.first() shouldBe Person(
			fornavn = "Steve",
			etternavn = "Mc Guffin",
			mellomnavn = null,
			telefonnummer = null,
			diskresjonskode = null
		)
	}

	test("Returnerer koordinator med mellomnavn om gjennomføringen har en koordinator") {
		createAnsatt(mellomnavn = "mellomnavn")
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
		koordinatorer.first() shouldBe person("Steve", "mellomnavn", "Mc Guffin")
	}

	test("Duplikater blir fjernet") {
		createAnsatt()
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		val ansattId2 = UUID.randomUUID()

		testDataRepository.insertArrangorAnsatt(
			ARRANGOR_ANSATT_1.copy(
				id = ansattId2,
				personligIdent = UUID.randomUUID().toString(),
				fornavn = "Steve",
				mellomnavn = null,
				etternavn = "Mc Guffin"
			)
		)

		createRolleForAnsatt("KOORDINATOR", ansattId2)
		createGjennomforingTilgang(ansattId2)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 1
	}

	test("Returnerer alle koordinatoerer for en gjennomføring") {
		createAnsatt()
		createRolleForAnsatt("KOORDINATOR")
		createGjennomforingTilgang()

		createAnsatt(
			baseInput = ARRANGOR_ANSATT_2,
			fornavn = "Jane",
			etternavn = "Doe"
		)

		createRolleForAnsatt("KOORDINATOR", ARRANGOR_ANSATT_2.id)
		createGjennomforingTilgang(ARRANGOR_ANSATT_2.id)

		val koordinatorer = query.query(GJENNOMFORING_1.id)
		koordinatorer.size shouldBe 2
		koordinatorer shouldContainAll setOf(
			person("Steve", "Mc Guffin"),
			person("Jane", "Doe")
		)

	}


})
