package no.nav.amt.tiltak.metrics

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class MetricRepositoryTest {
	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: MetricRepository
	lateinit var arrangorAnsattRepository: ArrangorAnsattRepository
	lateinit var testRepository: TestDataRepository

	@BeforeEach
	fun before() {

		repository = MetricRepository(NamedParameterJdbcTemplate(dataSource))
		arrangorAnsattRepository = ArrangorAnsattRepository(NamedParameterJdbcTemplate(dataSource))
		testRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun getSistInnloggetMetrics() {
		val metrics = repository.getSistInnloggetMetrics()

		metrics.antallAnsatte shouldBe 3
		metrics.antallAnsatteInnloggetSisteDag shouldBe 0

		arrangorAnsattRepository.setVelykketInnlogging(ARRANGOR_ANSATT_1.id)

		val updatedMetrics = repository.getSistInnloggetMetrics()

		updatedMetrics.antallAnsatteInnloggetSisteDag shouldBe 1
	}
	@Test
	internal fun getRolleInnloggetSisteTime() {
		val metrics = repository.getRolleInnloggetSisteTime()

		metrics.antallBegge shouldBe 0
		metrics.antallVeiledere shouldBe 0
		metrics.antallKoordinatorer shouldBe 0

	}
}
