package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository

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

	test("insertOgInaktiverStartDato - Ingen tidligere endringsmeldinger - inserter melding med alle verdier") {
		val now = LocalDate.now()
		val melding = repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		melding shouldNotBe null
		melding.deltakerId shouldBe DELTAKER_1.id
		melding.aktiv shouldBe true
		melding.opprettetAvId shouldBe ARRANGOR_ANSATT_1.id
		melding.startDato shouldBe now
	}

	test("insertOgInaktiverStartDato - Det finnes flere endringsmeldinger - inserter melding og inaktiverer den gamle") {
		val idag = LocalDate.now()
		val melding1 = repository.insertOgInaktiverStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		melding1.deltakerId shouldBe DELTAKER_1.id
		melding1.aktiv shouldBe true
		melding1.startDato shouldBe idag

		val nyDato = LocalDate.now().minusDays(1)
		val melding2 = repository.insertOgInaktiverStartDato(nyDato, DELTAKER_1.id, ARRANGOR_ANSATT_2.id)

		melding2 shouldNotBe null
		melding2.startDato shouldBe nyDato
		melding2.aktiv shouldBe true
		melding2.opprettetAvId shouldBe ARRANGOR_ANSATT_2.id

		val forrigeMelding = repository.get(melding1.id)

		melding1.copy(aktiv = false) shouldBe forrigeMelding
	}

	test("getByGjennomforing - en endringsmelding - henter endringsmelding") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforing_id)

		meldinger.size shouldBe 1
		meldinger.get(0).aktiv shouldBe true

	}

	test("getByGjennomforing - inaktiv endringsmelding - returnerer alle") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.insertOgInaktiverStartDato(now.minusDays(1), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforing_id)

		meldinger.size shouldBe 2
		meldinger.get(0).aktiv shouldBe false
		meldinger.get(1).aktiv shouldBe true
	}

})

