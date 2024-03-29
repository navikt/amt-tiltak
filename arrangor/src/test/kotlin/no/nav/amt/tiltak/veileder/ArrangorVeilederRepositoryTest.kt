package no.nav.amt.tiltak.veileder

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_3
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.UUID

class ArrangorVeilederRepositoryTest {

	lateinit var repository: ArrangorVeilederRepository

	lateinit var testDataRepository: TestDataRepository

	private val dataSource = SingletonPostgresContainer.getDataSource()

	@BeforeEach
	fun migrate() {
		repository = ArrangorVeilederRepository(NamedParameterJdbcTemplate(dataSource))
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `getAktiveForDeltaker - en aktiv og en inaktiv veileder - skal kun returnere aktiv veileder`() {
		val inaktivVeileder = ARRANGOR_ANSATT_2_VEILEDER_1.copy(gyldigTil = ZonedDateTime.now().minusWeeks(10))

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(inaktivVeileder)

		val veiledere = repository.getAktiveForDeltaker(DELTAKER_1.id)

		veiledere shouldHaveSize 1

		veiledere[0].id shouldBe ARRANGOR_ANSATT_1_VEILEDER_1.id
		veiledere[0].gyldigTil shouldBeGreaterThan ZonedDateTime.now()
	}

	@Test
	fun `inaktiverAlleVeiledereForDeltaker - en aktiv og en inaktiv veileder - gyldigTil endres på den aktive`() {
		val inaktivVeileder = ARRANGOR_ANSATT_2_VEILEDER_1.copy(gyldigTil = ZonedDateTime.now().minusWeeks(10))

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(inaktivVeileder)

		repository.inaktiverAlleVeiledereForDeltaker(DELTAKER_1.id)

		repository.getAktiveForDeltaker(DELTAKER_1.id) shouldHaveSize 0

		repository.get(inaktivVeileder.id).gyldigTil shouldBeCloseTo inaktivVeileder.gyldigTil

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()

	}

	@Test
	fun `inaktiverVeileder - aktiv veileder - gyldigTil settes til nå`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)

		repository.inaktiverVeileder(ARRANGOR_ANSATT_1_VEILEDER_1.ansattId, ARRANGOR_ANSATT_1_VEILEDER_1.deltakerId, ARRANGOR_ANSATT_1_VEILEDER_1.erMedveileder)

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()
	}

	@Test
	fun `inaktiverVeilederPaGjennomforinger - aktiv veileder finnes på flere gjennomforinger - den inaktiveres`() {
		val deltaker3 = DELTAKER_1.copy(
			id = UUID.randomUUID(),
			gjennomforingId = GJENNOMFORING_3.id
		)
		testDataRepository.insertGjennomforing(GJENNOMFORING_3)
		testDataRepository.insertDeltaker(deltaker3)

		val ansatt1Veileder2 = ARRANGOR_ANSATT_1_VEILEDER_1.copy(
			id = UUID.randomUUID(),
			deltakerId = deltaker3.id,
		)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ansatt1Veileder2)

		repository.inaktiverVeilederPaGjennomforinger(ARRANGOR_ANSATT_1.id, listOf(GJENNOMFORING_1.id, deltaker3.gjennomforingId))

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()
		repository.get(ansatt1Veileder2.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()
	}

	@Test
	fun `inaktiverVeilederaGjennomforinger - flere aktive veiledere finnes - kun de med riktig ansattId inaktiveres`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)

		repository.inaktiverVeilederPaGjennomforinger(ARRANGOR_ANSATT_1.id, listOf(GJENNOMFORING_1.id))

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()
		repository.get(ARRANGOR_ANSATT_2_VEILEDER_1.id).gyldigTil shouldBeCloseTo ARRANGOR_ANSATT_2_VEILEDER_1.gyldigTil
	}
}
