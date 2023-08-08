package no.nav.amt.tiltak.navansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.NavAnsattInput
import org.junit.AfterClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID

class NavAnsattRepositoryTest {
	companion object {
		private val dataSource = SingletonPostgresContainer.getDataSource()
		private val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
		private val testRepository = TestDataRepository(jdbcTemplate)
		private val repository = NavAnsattRepository(jdbcTemplate)

		@JvmStatic
		@AfterClass
		fun tearDown() {
			DbTestDataUtils.cleanDatabase(dataSource)
		}
	}

	@Test
	fun `get(uuid) - ansatt finnes - returnerer ansatt`() {
		val forventetAnsatt = mockAnsatt()
		testRepository.insertNavAnsatt(forventetAnsatt)
		val ansatt = repository.get(forventetAnsatt.id)

		sammenlign(ansatt, forventetAnsatt)
	}
	@Test
	fun `get(uuid) - ansatt finnes ikke - kaster NoSuchElementException`() {
		assertThrows<NoSuchElementException> {
			repository.get(UUID.randomUUID())
		}
	}

	@Test
	fun `get(string) - ansatt finnes - returnerer ansatt`() {
		val forventetAnsatt = mockAnsatt()
		testRepository.insertNavAnsatt(forventetAnsatt)
		val ansatt = repository.get(forventetAnsatt.navIdent)!!

		sammenlign(ansatt, forventetAnsatt)
	}
	@Test
	fun `get(string) - ansatt finnes ikke - returnerer null`() {
		repository.get("Ansatt som ikke finnes") shouldBe null
	}

	@Test
	fun `upsert - ny ansatt - inserter ansatt`() {
		val forventetAnsatt = mockAnsatt()
		repository.upsert(forventetAnsatt.toModel())
		val ansatt = repository.get(forventetAnsatt.id)

		sammenlign(ansatt, forventetAnsatt)
	}

	@Test
	fun `upsert - ansatt finnes - oppdaterer ansatt`() {
		val originalAnsatt = mockAnsatt()
		testRepository.insertNavAnsatt(originalAnsatt)

		val forventetAnsatt = originalAnsatt.copy(navn = "nytt navn", epost = "ny@epost", telefonnummer = "ny telefon")
		repository.upsert(forventetAnsatt.toModel())

		val ansatt = repository.get(forventetAnsatt.id)

		sammenlign(ansatt, forventetAnsatt)
	}

	@Test
	fun `finnesAnsatt - ansatt finnes - returnerer true`() {
		val forventetAnsatt = mockAnsatt()
		testRepository.insertNavAnsatt(forventetAnsatt)
		repository.finnesAnsatt(forventetAnsatt.id) shouldBe true
	}
	@Test
	fun `finnesAnsatt - ansatt finnes ikke - returnere false`() {
		repository.finnesAnsatt(UUID.randomUUID()) shouldBe false
	}

	private fun mockAnsatt() = NavAnsattInput(
		id = UUID.randomUUID(),
		navIdent = (1000 .. 9000).random().toString(),
		navn = "Veileder Veiledersen",
		epost = "veil@nav.no",
		telefonnummer = "2500052",
	)

	private fun sammenlign(faktisk: NavAnsattDbo, forventet: NavAnsattInput) {
		faktisk.id shouldBe forventet.id
		faktisk.navIdent shouldBe forventet.navIdent
		faktisk.navn shouldBe forventet.navn
		faktisk.telefonnummer shouldBe forventet.telefonnummer
		faktisk.epost shouldBe forventet.epost
	}


}
