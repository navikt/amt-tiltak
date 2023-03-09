package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.SkjultDeltakerInput
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

class SkjultDeltakerRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)

	val testRepository = TestDataRepository(jdbcTemplate)

	val repository = SkjultDeltakerRepository(jdbcTemplate)

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `skjulDeltaker - skal inserte skjult deltaker`() {
		repository.skjulDeltaker(UUID.randomUUID(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.erSkjultForTiltaksarrangor(listOf(DELTAKER_1.id))[DELTAKER_1.id] shouldBe true
	}

	@Test
	fun `opphevSkjulDeltaker - skal oppheve skjult deltaker`() {
		testRepository.insertSkjultDeltaker(SkjultDeltakerInput(UUID.randomUUID(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id, ZonedDateTime.now().plusDays(1)))

		repository.opphevSkjulDeltaker(DELTAKER_1.id)

		AsyncUtils.eventually(interval = Duration.ofMillis(10)) {
			repository.erSkjultForTiltaksarrangor(listOf(DELTAKER_1.id))[DELTAKER_1.id] shouldBe false
		}
	}

	@Test
	fun `erSkjultForTiltaksarrangor - skal hente om deltaker er skjult`() {
		testRepository.insertSkjultDeltaker(SkjultDeltakerInput(UUID.randomUUID(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id, ZonedDateTime.now().plusDays(1)))
		testRepository.insertSkjultDeltaker(SkjultDeltakerInput(UUID.randomUUID(), DELTAKER_2.id, ARRANGOR_ANSATT_1.id, ZonedDateTime.now().minusDays(1)))

		val erSkjultMap = repository.erSkjultForTiltaksarrangor(listOf(DELTAKER_1.id, DELTAKER_2.id))

		erSkjultMap[DELTAKER_1.id] shouldBe true
		erSkjultMap[DELTAKER_2.id] shouldBe false
	}

	@Test
	fun `slett - skal slette record`() {
		testRepository.insertSkjultDeltaker(SkjultDeltakerInput(UUID.randomUUID(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id, ZonedDateTime.now().plusDays(1)))

		repository.slett(DELTAKER_1.id)

		val erSkjult = repository.erSkjultForTiltaksarrangor(listOf(DELTAKER_1.id))

		erSkjult[DELTAKER_1.id] shouldBe false
	}

}
