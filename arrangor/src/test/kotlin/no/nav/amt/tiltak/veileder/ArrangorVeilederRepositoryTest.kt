package no.nav.amt.tiltak.veileder

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_4
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_4
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*

class ArrangorVeilederRepositoryTest {

	lateinit var repository: ArrangorVeilederRepository

	lateinit var testDataRepository: TestDataRepository

	private val dataSource = SingletonPostgresContainer.getDataSource()

	private val opprettVeilederDbo1 = OpprettVeilederDbo(
		ansattId = ARRANGOR_ANSATT_1.id,
		erMedveileder = false,
		gyldigFra = ZonedDateTime.now().minusSeconds(10),
		gyldigTil = ZonedDateTime.now().plusYears(5),
	)
	private val opprettVeilederDbo2 = OpprettVeilederDbo(
		ansattId = ARRANGOR_ANSATT_2.id,
		erMedveileder = true,
		gyldigFra = ZonedDateTime.now().minusSeconds(10),
		gyldigTil = ZonedDateTime.now().plusYears(5),
	)

	@BeforeEach
	fun migrate() {
		repository = ArrangorVeilederRepository(NamedParameterJdbcTemplate(dataSource))
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `opprettVeiledere - en veileder, medveileder og deltaker - insertes riktig`() {
		repository.opprettVeiledere(
			veiledere = listOf(opprettVeilederDbo1, opprettVeilederDbo2),
			deltakerIder = listOf(DELTAKER_1.id),
		)

		val veiledere = repository.getAktiveForDeltaker(DELTAKER_1.id)

		veiledere shouldHaveSize 2

		val veileder = veiledere.find { !it.erMedveileder }
		val medveileder = veiledere.find { it.erMedveileder }

		veileder!!.ansattId shouldBe opprettVeilederDbo1.ansattId
		veileder.deltakerId shouldBe DELTAKER_1.id
		veileder.gyldigFra shouldBeCloseTo opprettVeilederDbo1.gyldigFra
		veileder.gyldigTil shouldBeCloseTo opprettVeilederDbo1.gyldigTil

		medveileder!!.ansattId shouldBe opprettVeilederDbo2.ansattId
		medveileder.deltakerId shouldBe DELTAKER_1.id
		medveileder.gyldigFra shouldBeCloseTo opprettVeilederDbo2.gyldigFra
		medveileder.gyldigTil shouldBeCloseTo opprettVeilederDbo2.gyldigTil
	}

	@Test
	fun `opprettVeiledere - en veileder, medveileder og flere deltakere - insertes riktig`() {
		testDataRepository.insertBruker(BRUKER_4)
		testDataRepository.insertDeltaker(DELTAKER_4)

		val deltakere = listOf(DELTAKER_1.id, DELTAKER_2.id, DELTAKER_4.id)

		repository.opprettVeiledere(
			veiledere = listOf(opprettVeilederDbo1, opprettVeilederDbo2),
			deltakerIder = deltakere,
		)

		deltakere.forEach {deltakerId ->
			val veiledere = repository.getAktiveForDeltaker(deltakerId)

			veiledere shouldHaveSize 2

			val veileder = veiledere.find { !it.erMedveileder }
			val medveileder = veiledere.find { it.erMedveileder }

			veileder!!.ansattId shouldBe opprettVeilederDbo1.ansattId
			veileder.deltakerId shouldBe deltakerId
			veileder.gyldigFra shouldBeCloseTo opprettVeilederDbo1.gyldigFra
			veileder.gyldigTil shouldBeCloseTo opprettVeilederDbo1.gyldigTil

			medveileder!!.ansattId shouldBe opprettVeilederDbo2.ansattId
			medveileder.deltakerId shouldBe deltakerId
			medveileder.gyldigFra shouldBeCloseTo opprettVeilederDbo2.gyldigFra
			medveileder.gyldigTil shouldBeCloseTo opprettVeilederDbo2.gyldigTil
		}
	}

	@Test
	fun `inaktiverVeiledereForDeltakere - flere aktive veiledere på en deltaker - skal sette gyldigTil til nå på alle veilederne`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)

		repository.inaktiverVeiledereForDeltakere(
			ansattIder = listOf(ARRANGOR_ANSATT_1_VEILEDER_1.ansattId, ARRANGOR_ANSATT_2_VEILEDER_1.ansattId),
			deltakerIder = listOf(DELTAKER_1.id)
		)

		repository.getAktiveForDeltaker(DELTAKER_1.id) shouldHaveSize 0
	}

	@Test
	fun `inaktiverVeiledereForDeltakere - flere aktive veiledere på en deltaker - skal sette gyldigTil til nå på kun en veileder`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)

		repository.inaktiverVeiledereForDeltakere(
			ansattIder = listOf(ARRANGOR_ANSATT_1_VEILEDER_1.ansattId),
			deltakerIder = listOf(DELTAKER_1.id)
		)

		val veiledere = repository.getAktiveForDeltaker(DELTAKER_1.id)

		val inaktiveretVeileder = repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id)

		inaktiveretVeileder.gyldigTil shouldBeCloseTo ZonedDateTime.now()

		veiledere shouldHaveSize 1
		veiledere[0].id shouldBe ARRANGOR_ANSATT_2_VEILEDER_1.id
		veiledere[0].gyldigTil shouldBeCloseTo  ARRANGOR_ANSATT_2_VEILEDER_1.gyldigTil
	}

	@Test
	fun `inaktiverVeiledereForDeltakere - flere aktive veiledere på flere deltakere - skal sette gyldigTil til nå på alle veiledere`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(
			ARRANGOR_ANSATT_1_VEILEDER_1.copy(id = UUID.randomUUID(), deltakerId = DELTAKER_2.id)
		)
		testDataRepository.insertArrangorVeileder(
			ARRANGOR_ANSATT_2_VEILEDER_1.copy(id = UUID.randomUUID(), deltakerId = DELTAKER_2.id)
		)

		val deltakere = listOf(DELTAKER_1.id, DELTAKER_2.id)

		repository.inaktiverVeiledereForDeltakere(
			ansattIder = listOf(ARRANGOR_ANSATT_1_VEILEDER_1.ansattId, ARRANGOR_ANSATT_2_VEILEDER_1.ansattId),
			deltakerIder = deltakere,
		)

		repository.getAktiveForDeltakere(deltakere) shouldHaveSize 0
	}

	@Test
	fun `inaktiverVeiledereForDeltakere - en aktiv- og en inaktiv veileder på deltaker - skal ikke sette gyldigTil til nå på inaktiv veileder`() {
		val inaktivVeileder = ARRANGOR_ANSATT_2_VEILEDER_1.copy(gyldigTil = ZonedDateTime.now().minusWeeks(10))

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(inaktivVeileder)

		repository.inaktiverVeiledereForDeltakere(
			ansattIder = listOf(ARRANGOR_ANSATT_1_VEILEDER_1.ansattId, ARRANGOR_ANSATT_2_VEILEDER_1.ansattId),
			deltakerIder = listOf(DELTAKER_1.id)
		)

		repository.getAktiveForDeltaker(DELTAKER_1.id) shouldHaveSize 0

		repository.get(inaktivVeileder.id).gyldigTil shouldBeCloseTo inaktivVeileder.gyldigTil

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()
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
	fun `getAktiveForDeltakere - en aktiv og en inaktiv veileder for to deltakere - skal kun returnere aktiv veileder`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1.copy(deltakerId = DELTAKER_2.id))
		testDataRepository.insertArrangorVeileder(
			ARRANGOR_ANSATT_1_VEILEDER_1.copy(
				id = UUID.randomUUID(),
				deltakerId = DELTAKER_2.id,
				gyldigTil = ZonedDateTime.now().minusWeeks(5),
			)
		)
		testDataRepository.insertArrangorVeileder(
			ARRANGOR_ANSATT_2_VEILEDER_1.copy(
				id = UUID.randomUUID(),
				gyldigTil = ZonedDateTime.now().minusWeeks(5),
			)
		)

		val deltakere = listOf(DELTAKER_1.id, DELTAKER_2.id)

		val veiledere = repository.getAktiveForDeltakere(deltakere)

		veiledere shouldHaveSize 2

		val veileder1 = veiledere.first { it.deltakerId == DELTAKER_1.id }
		val veileder2 = veiledere.first { it.deltakerId == DELTAKER_2.id }

		veileder1.id shouldBe ARRANGOR_ANSATT_1_VEILEDER_1.id
		veileder1.gyldigTil shouldBeGreaterThan ZonedDateTime.now()

		veileder2.id shouldBe ARRANGOR_ANSATT_2_VEILEDER_1.id
		veileder2.gyldigTil shouldBeGreaterThan ZonedDateTime.now()
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
	fun `inaktiverVeiledere - en aktiv og en inaktiv veileder - gyldigTil endres på den aktive`() {
		val inaktivVeileder = ARRANGOR_ANSATT_2_VEILEDER_1.copy(gyldigTil = ZonedDateTime.now().minusWeeks(10))

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(inaktivVeileder)

		repository.inaktiverVeiledere(listOf(inaktivVeileder.id, ARRANGOR_ANSATT_1_VEILEDER_1.id))

		repository.get(inaktivVeileder.id).gyldigTil shouldBeCloseTo inaktivVeileder.gyldigTil

		repository.get(ARRANGOR_ANSATT_1_VEILEDER_1.id).gyldigTil shouldBeCloseTo ZonedDateTime.now()

	}
}
