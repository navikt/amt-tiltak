package no.nav.amt.tiltak.deltaker

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.deltaker.repositories.EndringsmeldingRepository
import no.nav.amt.tiltak.deltaker.service.EndringsmeldingService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

class EndringsmeldingServiceTest {
	lateinit var endringsmeldingService: EndringsmeldingService
	var dataSource = SingletonPostgresContainer.getDataSource()
	var repository: EndringsmeldingRepository = EndringsmeldingRepository(NamedParameterJdbcTemplate(dataSource))

	@BeforeEach
	fun beforeEach() {
		endringsmeldingService = EndringsmeldingService(repository)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `insertOgInaktiverStartDato - Inserter og inaktiverer forrige melding`() {
		val dato = LocalDate.now()

		var result1 = endringsmeldingService.insertOgInaktiverStartDato(dato, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		val result2 = endringsmeldingService.insertOgInaktiverStartDato(dato.minusDays(2), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		result1 = repository.get(result1.id)!!

		result1.aktiv shouldBe false
		result1.startDato shouldBe dato

		result2.aktiv shouldBe true
		result2.startDato shouldBe dato.minusDays(2)

	}

}
