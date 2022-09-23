package no.nav.amt.tiltak.endringsmelding

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.AuditEventSeverity
import no.nav.amt.tiltak.core.port.AuditEventType
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

class EndringsmeldingServiceImplTest {
	lateinit var endringsmeldingService: EndringsmeldingServiceImpl

	var dataSource = SingletonPostgresContainer.getDataSource()

	var jdbcTemplate = NamedParameterJdbcTemplate(dataSource)

	var repository = EndringsmeldingRepository(
		jdbcTemplate,
		TransactionTemplate(DataSourceTransactionManager(dataSource))
	)

	val auditLoggerService: AuditLoggerService = mockk()

	@BeforeEach
	fun beforeEach() {
		endringsmeldingService = EndringsmeldingServiceImpl(repository, auditLoggerService)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `opprettMedStartDato - Inserter og inaktiverer forrige melding`() {
		val dato = LocalDate.now()

		val melding1 = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, dato, ARRANGOR_ANSATT_1.id)
		val melding2 = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, dato.minusDays(2), ARRANGOR_ANSATT_1.id)

		val result1 = repository.get(melding1.id)
		val result2 = repository.get(melding2.id)

		result1.aktiv shouldBe false
		result1.startDato shouldBe dato

		result2.aktiv shouldBe true
		result2.startDato shouldBe dato.minusDays(2)
	}

	@Test
	fun `hentEndringsmeldinger - en endringsmelding på gjennomføring - returnerer dto med alle verdier satt`() {
		val nyStartDato = LocalDate.now().plusDays(3)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(DELTAKER_1.gjennomforingId)
		val endringsmelding = endringsmeldinger.get(0)

		endringsmeldinger.size shouldBe 1

		endringsmelding.deltakerId shouldBe DELTAKER_1.id
		endringsmelding.startDato shouldBe nyStartDato
		endringsmelding.aktiv shouldBe true
		endringsmelding.opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
		endringsmelding.ferdiggjortAvNavAnsattId shouldBe null
		endringsmelding.ferdiggjortTidspunkt shouldBe null
		endringsmelding.aktiv shouldBe true
		//endringsmelding.opprettet shouldBe null
	}

	@Test
	fun `hentEndringsmeldinger - flere endringsmeldinger på gjennomføring`() {
		val nyStartDato = LocalDate.now().plusDays(3)

		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)
		endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, nyStartDato, ARRANGOR_ANSATT_1.id)

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(DELTAKER_1.gjennomforingId)
		val aktivEndringsmelding = endringsmeldinger.filter { it.aktiv }
		val arkiverteEndringsmeldinger = endringsmeldinger.filter { it.ferdiggjortAvNavAnsattId != null || !it.aktiv }

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

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(GJENNOMFORING_2.id)

		endringsmeldinger.size shouldBe 0
	}

	@Test
	fun `markerSomFerdig - skal markere melding som ferdig og audit logge`() {
		val endringsmelding = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, LocalDate.now(), ARRANGOR_ANSATT_1.id)

		every {
			auditLoggerService.navAnsattAuditLog(any(), any(), any(), any(), any())
		} returns Unit

		endringsmeldingService.markerSomFerdig(endringsmelding.id, NAV_ANSATT_1.id)

		val ferdigMelding = endringsmeldingService.hentEndringsmelding(endringsmelding.id)

		ferdigMelding.aktiv shouldBe false
		ferdigMelding.ferdiggjortAvNavAnsattId shouldBe NAV_ANSATT_1.id
		ferdigMelding.ferdiggjortTidspunkt shouldNotBe null

		verify(exactly = 1) {
			auditLoggerService.navAnsattAuditLog(
				NAV_ANSATT_1.id,
				DELTAKER_1.id,
				AuditEventType.ACCESS,
				AuditEventSeverity.INFO,
				"NAV-ansatt har lest melding fra tiltaksarrangoer om oppstartsdato paa tiltak for aa registrere dette."
			)
		}
	}

	@Test
	fun `hentAntallAktiveForGjennomforing - teller aktive endringsmeldinger pa gjennomforing`() {
		val dato = LocalDate.now()
		val melding1 = endringsmeldingService.opprettMedStartDato(DELTAKER_1.id, dato, ARRANGOR_ANSATT_1.id)
		val melding2 = endringsmeldingService.opprettMedStartDato(DELTAKER_2.id, dato.minusDays(2), ARRANGOR_ANSATT_1.id)
		val gjennomforingId = DELTAKER_1.gjennomforingId

		every {
			auditLoggerService.navAnsattAuditLog(any(), any(), any(), any(), any())
		} returns Unit

		endringsmeldingService.hentAntallAktiveForGjennomforing(gjennomforingId) shouldBe 2

		endringsmeldingService.markerSomFerdig(melding1.id, NAV_ANSATT_1.id)
		endringsmeldingService.hentAntallAktiveForGjennomforing(gjennomforingId) shouldBe 1

		endringsmeldingService.markerSomFerdig(melding2.id, NAV_ANSATT_1.id)
		endringsmeldingService.hentAntallAktiveForGjennomforing(gjennomforingId) shouldBe 0
	}


}
