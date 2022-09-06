package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.deltaker.dbo.DeltakerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpdateDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class DeltakerRepositoryIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var repository: DeltakerRepository

	@Autowired
	lateinit var deltakerStatusRepository: DeltakerStatusRepository

	private val now = LocalDate.now().atStartOfDay()

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `Insert should insert Deltaker and return DeltakerDbo`() {
		val id = UUID.randomUUID()
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f
		val begrunnelse = "begrunnelse"

		repository.insert(
			DeltakerInsertDbo(
				id,
				TestData.BRUKER_3.id,
				TestData.GJENNOMFORING_1.id,
				startDato,
				sluttDato,
				dagerPerUke,
				prosentStilling,
				registrertDato,
				begrunnelse
			)
		)
		val dbo = repository.get(id)

		dbo shouldNotBe null
		dbo!!.id shouldBe id
		dbo.brukerId shouldBe TestData.BRUKER_3.id
		dbo.brukerFornavn shouldBe TestData.BRUKER_3.fornavn
		dbo.brukerEtternavn shouldBe TestData.BRUKER_3.etternavn
		dbo.brukerFodselsnummer shouldBe TestData.BRUKER_3.fodselsnummer
		dbo.gjennomforingId shouldBe TestData.GJENNOMFORING_1.id
		dbo.startDato shouldBe startDato
		dbo.sluttDato shouldBe sluttDato
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
		dbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe registrertDato.truncatedTo(ChronoUnit.MINUTES)
		dbo.innsokBegrunnelse shouldBe begrunnelse
	}

	@Test
	internal fun `Update should update Deltaker and return the updated Deltaker`() {
		val nyStartdato = LocalDate.now().plusDays(1)
		val nySluttdato = LocalDate.now().plusDays(14)
		val nyBegrunnelse = "ny begrunnelse"

		val updatedDeltaker = repository.update(
			DeltakerUpdateDbo(
				id = TestData.DELTAKER_1.id,
				startDato = nyStartdato,
				sluttDato = nySluttdato,
				registrertDato = LocalDateTime.now(),
				innsokBegrunnelse = nyBegrunnelse
			)
		)

		updatedDeltaker.id shouldBe TestData.DELTAKER_1.id
		updatedDeltaker.startDato shouldBe nyStartdato
		updatedDeltaker.sluttDato shouldBe nySluttdato
		updatedDeltaker.innsokBegrunnelse shouldBe nyBegrunnelse
	}

	@Test
	internal fun `Get by id`() {
		val insertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().plusDays(7),
			null,
			2,
			20.0f,
			now
		)

		repository.insert(insertDbo)

		val gottenDbo = repository.get(insertDbo.id)

		gottenDbo shouldNotBe null
		gottenDbo!!.id shouldBe insertDbo.id
		gottenDbo.brukerId shouldBe insertDbo.brukerId
		gottenDbo.gjennomforingId shouldBe insertDbo.gjennomforingId
		gottenDbo.startDato shouldBe insertDbo.startDato
		gottenDbo.sluttDato shouldBe insertDbo.sluttDato
		gottenDbo.dagerPerUke shouldBe insertDbo.dagerPerUke
		gottenDbo.prosentStilling shouldBe insertDbo.prosentStilling
		gottenDbo.registrertDato shouldBe insertDbo.registrertDato
	}

	@Test
	internal fun `Get by BrukerId and Gjennomforing`() {
		val insertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().plusDays(7),
			null,
			2,
			20.0f,
			now
		)

		repository.insert(insertDbo)

		val gottenDbo = repository.get(insertDbo.brukerId, insertDbo.gjennomforingId)

		gottenDbo shouldNotBe null
		gottenDbo!!.id shouldBe insertDbo.id
		gottenDbo.brukerId shouldBe insertDbo.brukerId
		gottenDbo.gjennomforingId shouldBe insertDbo.gjennomforingId
		gottenDbo.startDato shouldBe insertDbo.startDato
		gottenDbo.sluttDato shouldBe insertDbo.sluttDato
		gottenDbo.dagerPerUke shouldBe insertDbo.dagerPerUke
		gottenDbo.prosentStilling shouldBe insertDbo.prosentStilling
		gottenDbo.registrertDato shouldBe insertDbo.registrertDato

	}

	@Test
	internal fun `Get by Fodselsnummer and Gjennomforing`() {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f
		val gjennomforing = TestData.GJENNOMFORING_1
		val bruker = TestData.BRUKER_3

		val insertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			bruker.id,
			gjennomforing.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		repository.insert(insertDbo)

		val gottenDbo = repository.get(bruker.fodselsnummer, gjennomforing.id)

		gottenDbo shouldNotBe null
		gottenDbo!!.gjennomforingId shouldBe gjennomforing.id
		gottenDbo.brukerId shouldBe bruker.id

	}

	@Test
	internal fun `potensieltHarSlutta - status DELTAR og sluttdato passert - deltaker returneres`() {
		val deltakerId = UUID.randomUUID()

		val deltakerInsertDbo = DeltakerInsertDbo(
			deltakerId,
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().minusDays(7),
			LocalDate.now().minusDays(2),
			2,
			20.0f,
			now.minusDays(10)
		)
		repository.insert(deltakerInsertDbo)
		val deltaker = repository.get(deltakerInsertDbo.id)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltaker!!.id,
			type = Deltaker.Status.DELTAR,
			gyldigFra = LocalDateTime.now().minusDays(5))

		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltHarSlutta = repository.potensieltHarSlutta().filter { it.id == deltakerId }

		potensieltHarSlutta shouldHaveSize 1
		potensieltHarSlutta[0] shouldBe deltaker

	}

	@Test
	internal fun `potensieltHarSlutta - status DELTAR og sluttdato ikke passert - deltaker returneres ikke`() {
		val deltakerInsertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().minusDays(7),
			LocalDate.now().plusDays(2),
			2,
			20.0f,
			LocalDateTime.now().minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = Deltaker.Status.DELTAR,
			gyldigFra = LocalDateTime.now().minusDays(5),
		)
		repository.insert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltHarSlutta = repository.potensieltHarSlutta().filter { it.id == TestData.BRUKER_3.id }

		potensieltHarSlutta shouldHaveSize 0
	}

	@Test
	internal fun `potensieltDeltar - startdato passert og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres`() {
		val deltakerInsertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().minusDays(2),
			LocalDate.now().plusDays(10),
			2,
			20.0f,
			now.minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = Deltaker.Status.VENTER_PA_OPPSTART,
			gyldigFra = now.minusDays(5)
		)
		repository.insert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltDeltar = repository.potensieltDeltar()

		potensieltDeltar shouldHaveSize 1
		potensieltDeltar[0].id shouldBe deltakerInsertDbo.id
		potensieltDeltar[0].brukerId shouldBe deltakerInsertDbo.brukerId
		potensieltDeltar[0].gjennomforingId shouldBe deltakerInsertDbo.gjennomforingId
		potensieltDeltar[0].startDato shouldBe deltakerInsertDbo.startDato
		potensieltDeltar[0].sluttDato shouldBe deltakerInsertDbo.sluttDato
		potensieltDeltar[0].dagerPerUke shouldBe deltakerInsertDbo.dagerPerUke
		potensieltDeltar[0].prosentStilling shouldBe deltakerInsertDbo.prosentStilling
		potensieltDeltar[0].registrertDato shouldBe deltakerInsertDbo.registrertDato

	}

	@Test
	internal fun `potensieltDeltar - startdato og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres ikke`() {
		val deltakerInsertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().plusDays(2),
			LocalDate.now().plusDays(10),
			2,
			20.0f,
			LocalDateTime.now().minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = Deltaker.Status.VENTER_PA_OPPSTART,
			gyldigFra = LocalDateTime.now().minusDays(5),
		)

		repository.insert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltDeltar = repository.potensieltDeltar()

		potensieltDeltar shouldHaveSize 0

	}

	@Test
	internal fun `slettDeltaker skal slette deltaker`() {
		val deltakerInsertDbo = DeltakerInsertDbo(
			UUID.randomUUID(),
			TestData.BRUKER_3.id,
			TestData.GJENNOMFORING_1.id,
			LocalDate.now().plusDays(2),
			LocalDate.now().plusDays(10),
			2,
			20.0f,
			LocalDateTime.now().minusDays(10)
		)

		repository.insert(deltakerInsertDbo)
		repository.get(deltakerInsertDbo.id) shouldNotBe null
		repository.slettDeltaker(deltakerInsertDbo.id)
		repository.get(deltakerInsertDbo.id) shouldBe null

	}
}
