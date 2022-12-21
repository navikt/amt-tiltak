package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.isEqualTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.GjennomforingInput
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class GjennomforingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: GjennomforingRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = GjennomforingRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("insert() should insert gjennomforing and return object") {
		val id = UUID.randomUUID()
		val navn = "TEST Tiltaksgjennomforing"
		val status = Gjennomforing.Status.IKKE_STARTET
		val startDato = LocalDate.now().plusDays(2)
		val sluttDato = LocalDate.now().plusDays(10)

		val savedGjennomforing = repository.insert(
			id = id,
			tiltakId = TILTAK_1.id,
			arrangorId = ARRANGOR_1.id,
			navn = navn,
			status = status,
			startDato = startDato,
			sluttDato = sluttDato,
			navEnhetId = NAV_ENHET_1.id,
			opprettetAar = 2020,
			lopenr = 123,
			deprecated = true,
		)

		assertNotNull(savedGjennomforing)
		assertNotNull(savedGjennomforing.id)

		assertEquals(TILTAK_1.id, savedGjennomforing.tiltakId)
		assertEquals(ARRANGOR_1.id, savedGjennomforing.arrangorId)
		assertEquals(navn, savedGjennomforing.navn)
		assertEquals(status, savedGjennomforing.status)

		assertTrue(startDato.isEqualTo(savedGjennomforing.startDato!!))
		assertTrue(sluttDato.isEqualTo(savedGjennomforing.sluttDato!!))
		assertEquals(2020, savedGjennomforing.opprettetAar)
		assertEquals(123, savedGjennomforing.lopenr)

		assertTrue(savedGjennomforing.deprecated)
	}

	test("update() should throw if gjennomforing does not exist") {
		assertThrows<NoSuchElementException> {
			repository.update(
				GjennomforingDbo(
					id = UUID.randomUUID(),
					arrangorId = UUID.randomUUID(),
					tiltakId = UUID.randomUUID(),
					navn = "idosfja",
					status = Gjennomforing.Status.IKKE_STARTET,
					startDato = null,
					sluttDato = null,
					navEnhetId = null,
					createdAt = LocalDateTime.now(),
					modifiedAt = LocalDateTime.now(),
					lopenr = 123,
					opprettetAar = 2020
				)
			)
		}
	}

	test("update() should return updated object") {
		val updatedNavn = "UpdatedNavn"
		val updatedStatus = Gjennomforing.Status.GJENNOMFORES
		val updatedStartDato = LocalDate.now().plusDays(4)
		val updatedSluttDato = LocalDate.now().plusDays(14)

		val updatedGjennomforing = repository.update(
			GjennomforingDbo(
				id = GJENNOMFORING_1.id,
				arrangorId = ARRANGOR_1.id,
				tiltakId = TILTAK_1.id,
				navn = updatedNavn,
				status = updatedStatus,
				startDato = updatedStartDato,
				sluttDato = updatedSluttDato,
				navEnhetId = null,
				createdAt = LocalDateTime.now(),
				modifiedAt = LocalDateTime.now(),
				lopenr = 90879,
				opprettetAar = 2030
			)
		)

		assertEquals(updatedNavn, updatedGjennomforing.navn)
		assertEquals(updatedStatus, updatedGjennomforing.status)
		assertTrue(updatedStartDato.isEqualTo(updatedGjennomforing.startDato))
		assertTrue(updatedSluttDato.isEqualTo(updatedGjennomforing.sluttDato))
		assertEquals(90879, updatedGjennomforing.lopenr)
		assertEquals(2030, updatedGjennomforing.opprettetAar)
	}

	test("delete should delete gjennomføring") {
		val id = UUID.randomUUID()

		val gjennomforing = GjennomforingInput(
			id = id,
			tiltakId = TILTAK_1.id,
			arrangorId = ARRANGOR_1.id,
			navn = "Tiltaksgjennomforing",
			status = "GJENNOMFORES",
			startDato = LocalDate.of(2022, 2, 1),
			sluttDato = LocalDate.of(2050, 12, 30),
			navEnhetId = null,
			lopenr = 123,
			opprettetAar = 2020
		)

		TestDataRepository(NamedParameterJdbcTemplate(dataSource))
			.insertGjennomforing(gjennomforing)

		repository.get(id) shouldNotBe null

		repository.delete(id)

		repository.get(id) shouldBe null
	}

	test("get - skal hente flere gjennomføringer") {
		val gjennomforinger = repository.get(listOf(GJENNOMFORING_1.id, GJENNOMFORING_2.id))

		gjennomforinger shouldHaveSize 2
		gjennomforinger.find { it.id == GJENNOMFORING_1.id } shouldNotBe null
		gjennomforinger.find { it.id == GJENNOMFORING_2.id } shouldNotBe null
	}

	test("get - skal returnere tom liste hvis ingen gjennomføringer sendes inn") {
		val gjennomforinger = repository.get(emptyList())

		gjennomforinger shouldHaveSize 0
	}

	test("getByArrangorId - skal hente gjennomforinger hos arrangor") {
		val gjennomforinger = repository.getByArrangorId(ARRANGOR_1.id)

		gjennomforinger shouldHaveSize 1
		gjennomforinger.forEach { it.arrangorId shouldBe ARRANGOR_1.id }
	}
})

