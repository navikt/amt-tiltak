package no.nav.amt.tiltak.deltaker

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import no.nav.amt.tiltak.ansatt.ArrangorAnsattServiceImpl
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.EndringsmeldingRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import no.nav.amt.tiltak.deltaker.service.EndringsmeldingService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_KONTOR_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

class EndringsmeldingServiceTest {
	lateinit var endringsmeldingService: EndringsmeldingService
	var dataSource = SingletonPostgresContainer.getDataSource()
	var jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	var repository: EndringsmeldingRepository = EndringsmeldingRepository(jdbcTemplate)

	var brukerRepository = BrukerRepository(jdbcTemplate)
	var navKontorRepository = NavKontorRepository(jdbcTemplate)
	var arrangorAnsattRepository = ArrangorAnsattRepository(jdbcTemplate)
	var arrangorAnsattService = ArrangorAnsattServiceImpl(arrangorAnsattRepository,mockk<PersonService>(), jdbcTemplate)

	@BeforeEach
	fun beforeEach() {
		endringsmeldingService = EndringsmeldingService(repository, brukerRepository, navKontorRepository, arrangorAnsattService)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `opprettMedStartDato - Inserter og inaktiverer forrige melding`() {
		val dato = LocalDate.now()

		var result1 = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, dato, ARRANGOR_ANSATT_1.id)

		val result2 = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, dato.minusDays(2), ARRANGOR_ANSATT_1.id)
		result1 = repository.get(result1.id)!!

		result1.aktiv shouldBe false
		result1.startDato shouldBe dato

		result2.aktiv shouldBe true
		result2.startDato shouldBe dato.minusDays(2)

	}

	@Test
	fun `hentEndringsmeldinger - en endringsmelding på gjennomføring - returnerer dto med alle verdier satt`() {
		val nyStartDato = LocalDate.now().plusDays(3)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldinger(DELTAKER_1.gjennomforing_id)
		val endringsmelding = endringsmeldinger.get(0)

		endringsmeldinger.size shouldBe 1

		endringsmelding.bruker.fornavn shouldBe BRUKER_1.fornavn
		endringsmelding.bruker.etternavn shouldBe BRUKER_1.etternavn
		endringsmelding.startDato shouldBe nyStartDato
		endringsmelding.aktiv shouldBe true
		endringsmelding.arkivert shouldBe false
		endringsmelding.godkjent shouldBe false
		endringsmelding.opprettetAvArrangorAnsatt.fornavn shouldBe ARRANGOR_ANSATT_1.fornavn
		endringsmelding.opprettetAvArrangorAnsatt.etternavn shouldBe ARRANGOR_ANSATT_1.etternavn
		endringsmelding.bruker.navKontor?.navn shouldBe NAV_KONTOR_1.navn

	}

	@Test
	fun `hentEndringsmeldinger - flere endringsmeldinger på gjennomføring`() {
		val nyStartDato = LocalDate.now().plusDays(3)

		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldinger(DELTAKER_1.gjennomforing_id)
		val aktivEndringsmelding = endringsmeldinger.filter { it.aktiv }
		val arkiverteEndringsmeldinger = endringsmeldinger.filter { it.arkivert }

		endringsmeldinger.size shouldBe 3
		aktivEndringsmelding.size shouldBe 1
		arkiverteEndringsmeldinger.size shouldBe 2

	}

	@Test
	fun `hentEndringsmeldinger - endringsmeldinger på andre gjennomføringer - returnerer ingen`() {
		val nyStartDato = LocalDate.now().plusDays(3)

		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldinger(GJENNOMFORING_2.id)

		endringsmeldinger.size shouldBe 0

	}

}
