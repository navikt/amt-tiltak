package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

internal class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	beforeEach {

		repository = EndringsmeldingRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("insertOgInaktiver - Ingen tidligere endringsmeldinger - inserter melding med alle verdier") {
		val now = LocalDate.now()
		val melding = repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		melding shouldNotBe null
		melding.deltakerId shouldBe DELTAKER_1.id
		melding.aktiv shouldBe true
		melding.opprettetAv shouldBe ARRANGOR_ANSATT_1.id
		melding.sluttDato shouldBe null
		melding.startDato shouldBe now
	}

	test("insertOgInaktiver - Det finnes flere endringsmeldinger - inserter melding og inaktiverer den gamle") {
		val idag = LocalDate.now()
		val forsteMelding = repository.insertOgInaktiverStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		forsteMelding.deltakerId shouldBe DELTAKER_1.id
		forsteMelding.aktiv shouldBe true
		forsteMelding.startDato shouldBe idag

		val nyDato = LocalDate.now().minusDays(1)
		val melding2 = repository.insertOgInaktiverStartDato(nyDato, DELTAKER_1.id, ARRANGOR_ANSATT_2.id)

		melding2 shouldNotBe null
		melding2.startDato shouldBe nyDato
		melding2.aktiv shouldBe true
		melding2.opprettetAv shouldBe ARRANGOR_ANSATT_2.id

		val forrigeMelding = repository.get(forsteMelding.id)

		forsteMelding.copy(aktiv = false) shouldBe forrigeMelding

	}
})

