package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds

class ArrangorAnsattGjennomforingTilgangServiceTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorAnsattGjennomforingTilgangRepository

	lateinit var testRepository: TestDataRepository

	lateinit var transactionTemplate: TransactionTemplate

	lateinit var service: ArrangorAnsattGjennomforingTilgangService

	lateinit var gjennomforingService: GjennomforingService

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val template = NamedParameterJdbcTemplate(dataSource)

		repository = ArrangorAnsattGjennomforingTilgangRepository(template)

		testRepository = TestDataRepository(template)

		transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

		gjennomforingService = mockk()

		service = ArrangorAnsattGjennomforingTilgangService(repository, gjennomforingService, transactionTemplate)


		DbTestDataUtils.cleanDatabase(dataSource)

		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)
		testRepository.insertGjennomforing(
			GJENNOMFORING_2.copy(
				arrangorId = ARRANGOR_1.id,
				navEnhetId = NAV_ENHET_1.id
			)
		)
	}

	test("fjernTilgang - skal fjerne tilgang til gjennomføring") {
		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_2.id
			)
		)

		service.fjernTilgang(ARRANGOR_ANSATT_1.id, GJENNOMFORING_1.id)

		eventually(5.seconds) {
			val aktiveTilganger =
				repository.hentAktiveGjennomforingTilgangerForAnsatt(ARRANGOR_ANSATT_1.id)

			aktiveTilganger shouldHaveSize 1
			aktiveTilganger.first().gjennomforingId shouldBe GJENNOMFORING_2.id
		}
	}

	test("opprettTilgang - skal kaste exception hvis tilgang er allerede opprettet") {
		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)

		shouldThrowExactly<IllegalStateException> {
			service.opprettTilgang(UUID.randomUUID(), ARRANGOR_ANSATT_1.id, GJENNOMFORING_1.id)
		}
	}

	test("fjernTilgangTilGjennomforinger - skal fjerne tilgang til gjennomforing hos arrangor") {
		val id1 = UUID.randomUUID()
		val id2 = UUID.randomUUID()

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = id1,
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id
			)
		)

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = id2,
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_2.id
			)
		)

		every { gjennomforingService.getByArrangorId(ARRANGOR_1.id) } returns listOf(
			Gjennomforing(
				id = GJENNOMFORING_1.id,
				tiltak = mockk(),
				arrangor = mockk(),
				navn = "",
				status = Gjennomforing.Status.GJENNOMFORES,
				startDato = null,
				sluttDato = null,
				registrertDato = LocalDateTime.now(),
				fremmoteDato = null,
				navEnhetId = null,
				opprettetAar = null,
				lopenr = null,
			), Gjennomforing(
				id = GJENNOMFORING_2.id,
				tiltak = mockk(),
				arrangor = mockk(),
				navn = "",
				status = Gjennomforing.Status.GJENNOMFORES,
				startDato = null,
				sluttDato = null,
				registrertDato = LocalDateTime.now(),
				fremmoteDato = null,
				navEnhetId = null,
				opprettetAar = null,
				lopenr = null,
		))

		var aktiveGjennomforingTilganger = repository.hentAktiveGjennomforingTilgangerForAnsatt(ARRANGOR_ANSATT_1.id)
		aktiveGjennomforingTilganger shouldHaveSize 2

		service.fjernTilgangTilGjennomforinger(ARRANGOR_ANSATT_1.id, ARRANGOR_1.id)

		aktiveGjennomforingTilganger = repository.hentAktiveGjennomforingTilgangerForAnsatt(ARRANGOR_ANSATT_1.id)
		aktiveGjennomforingTilganger shouldHaveSize 0
	}

})
