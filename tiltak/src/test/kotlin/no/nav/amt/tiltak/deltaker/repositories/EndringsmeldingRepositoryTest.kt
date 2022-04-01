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

	test("insertNyStartDato - Ingen tidligere endringsmeldinger - inserter melding med alle verdier") {
		val now = LocalDate.now()
		val melding = repository.insertNyStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.inaktiverTidligereMeldinger(DELTAKER_1.id)

		melding shouldNotBe null
		melding.deltakerId shouldBe DELTAKER_1.id
		melding.aktiv shouldBe true
		melding.opprettetAv shouldBe ARRANGOR_ANSATT_1.id
		melding.sluttDato shouldBe null
		melding.startDato shouldBe now
	}

	test("insertNyStartDato - Det finnes flere endringsmeldinger - inserter melding og inaktiverer den gamle") {
		val idag = LocalDate.now()
		val melding1 = repository.insertNyStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.inaktiverTidligereMeldinger(DELTAKER_1.id)

		melding1.deltakerId shouldBe DELTAKER_1.id
		melding1.aktiv shouldBe true
		melding1.startDato shouldBe idag
		melding1.sluttDato shouldBe null

		val nyDato = LocalDate.now().minusDays(1)
		val melding2 = repository.insertNyStartDato(nyDato, DELTAKER_1.id, ARRANGOR_ANSATT_2.id)

		melding2 shouldNotBe null
		melding2.startDato shouldBe nyDato
		melding2.aktiv shouldBe true
		melding2.opprettetAv shouldBe ARRANGOR_ANSATT_2.id

		val forrigeMelding = repository.get(melding1.id)

		melding1.copy(aktiv = false) shouldBe forrigeMelding
	}

	test("inaktiverTidligereMeldinger - Det finnes flere endringsmeldinger - Deaktiverer alle") {
		val idag = LocalDate.now()
		val melding1 = repository.insertNyStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		val melding2 = repository.insertNyStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_2.id)
		repository.inaktiverTidligereMeldinger(DELTAKER_1.id)

		val melding1Inaktivert = repository.get(melding1.id)
		val melding2Inaktivert = repository.get(melding2.id)

		melding1Inaktivert!!.aktiv shouldBe false
		melding2Inaktivert!!.aktiv shouldBe false


	}
})

