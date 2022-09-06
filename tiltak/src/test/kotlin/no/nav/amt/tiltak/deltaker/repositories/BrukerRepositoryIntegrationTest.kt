package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BrukerRepositoryIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var repository: BrukerRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `Insert should insert bruker and return BrukerDbo`() {
		val fodselsnummer = "64798632"
		val fornavn = "Per"
		val mellomnavn = null
		val etternavn = "Testersen"
		val telefonnummer = "74635462"
		val epost = "per.testersen@test.no"
		val ansvarligVeilederId = TestData.NAV_ANSATT_1.id
		val bruker = BrukerInsertDbo(
			fodselsnummer,
			fornavn,
			mellomnavn,
			etternavn,
			telefonnummer,
			epost,
			ansvarligVeilederId,
			null
		)
		val dbo = repository.insert(bruker)

		dbo shouldNotBe null
		dbo.id shouldNotBe null
		dbo.fodselsnummer shouldBe fodselsnummer
		dbo.fornavn shouldBe fornavn
		dbo.etternavn shouldBe etternavn
		dbo.telefonnummer shouldBe telefonnummer
		dbo.epost shouldBe epost
		dbo.ansvarligVeilederId shouldBe ansvarligVeilederId
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
	}

	@Test
	fun `Get user that does not exist should be null`() {
		repository.get("234789") shouldBe null
	}

	@Test
	fun `oppdaterVeileder should update veileder`() {
		repository.get(TestData.BRUKER_1.fodselsnummer)?.ansvarligVeilederId shouldBe TestData.NAV_ANSATT_1.id

		repository.oppdaterVeileder(TestData.BRUKER_1.fodselsnummer, TestData.NAV_ANSATT_2.id)

		repository.get(TestData.BRUKER_1.fodselsnummer)?.ansvarligVeilederId shouldBe TestData.NAV_ANSATT_2.id
	}


}
