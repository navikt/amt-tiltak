package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.metrics

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_3
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1_GJENNOMFORING_1_TILGANG
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class TiltaksansvarligGjennomforingTilgangMetricRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	private lateinit var repository: TiltaksansvarligGjennomforingTilgangMetricRepository

	lateinit var testDataRepository: TestDataRepository

	private val deltaker1 = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_1.id)
	private val deltaker2 = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_2.id)
	private val deltaker3 = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_3.id)

	@BeforeEach
	fun setup() {
		val template = NamedParameterJdbcTemplate(dataSource)
		repository = TiltaksansvarligGjennomforingTilgangMetricRepository(template)
		testDataRepository = TestDataRepository(template)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)

		testDataRepository.deleteAllTiltaksansvarligGjennomforingTilgang()
		testDataRepository.deleteAllEndringsmeldinger()

		testDataRepository.insertGjennomforing(GJENNOMFORING_3)

		testDataRepository.insertDeltaker(deltaker1)
		testDataRepository.insertDeltaker(deltaker2)
		testDataRepository.insertDeltaker(deltaker3)

		testDataRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltaker1.id,
				status = Endringsmelding.Status.AKTIV,
			)
		)
		testDataRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltaker2.id,
				status = Endringsmelding.Status.AKTIV,
			)
		)
		testDataRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltaker3.id,
				status = Endringsmelding.Status.UTFORT,
			)
		)
	}

	@Test
	fun `antallGjennomforingerUtenTilgangerMedMeldinger - skal telle antall unike gjennomforinger med aktive meldinger uten aktive tilganger - ingen tilganger finnes`() {
		val antall = repository.antallGjennomforingerUtenTilgangerMedMeldinger()

		antall shouldBe 2
	}

	@Test
	fun `antallGjennomforingerUtenTilgangerMedMeldinger - skal telle antall unike gjennomforinger med aktive meldinger uten aktive tilganger - tilganger finnes`() {
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(NAV_ANSATT_1_GJENNOMFORING_1_TILGANG)
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			NAV_ANSATT_1_GJENNOMFORING_1_TILGANG.copy(
				id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_2.id
			)
		)
		val antall = repository.antallGjennomforingerUtenTilgangerMedMeldinger()

		antall shouldBe 0
	}

	@Test
	fun `antallGjennomforingerUtenTilgangerMedMeldinger - skal telle antall unike gjennomforinger med aktive med aktive meldinger uten aktive tilganger - skal kun telle gjennomforing en gang`() {
		testDataRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltaker1.id,
				status = Endringsmelding.Status.AKTIV,
			)
		)
		val antall = repository.antallGjennomforingerUtenTilgangerMedMeldinger()

		antall shouldBe 2
	}
}
