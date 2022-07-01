package no.nav.amt.tiltak.tilgangskontroll.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattGjennomforingTilgang
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*

class ArrangorAnsattGjennomforingTilgangRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorAnsattGjennomforingTilgangRepository

	lateinit var testRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val parameterTemplate = NamedParameterJdbcTemplate(dataSource)

		repository = ArrangorAnsattGjennomforingTilgangRepository(parameterTemplate)
		testRepository = TestDataRepository(parameterTemplate)

		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("opprettTilgang skal opprette tilgang") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)

		val tilgangId = UUID.randomUUID()

		val gyldigFra = ZonedDateTime.now()
		val gyldigTil = ZonedDateTime.now().plusHours(1)

		repository.opprettTilgang(tilgangId, ARRANGOR_ANSATT_1.id, GJENNOMFORING_1.id, gyldigFra, gyldigTil)

		val tilgang = repository.get(tilgangId)

		tilgang.id shouldBe tilgangId
		tilgang.gjennomforingId shouldBe GJENNOMFORING_1.id
		tilgang.gyldigFra shouldBeEqualTo gyldigFra
		tilgang.gyldigTil shouldBeEqualTo gyldigTil
		tilgang.ansattId shouldBe ARRANGOR_ANSATT_1.id
	}

	test("hentAktiveGjennomforingTilgangerForAnsatt - skal returnere tilganger som ikke er stoppet") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertNavEnhet(NAV_ENHET_2)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangor(ARRANGOR_2)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)
		testRepository.insertGjennomforing(GJENNOMFORING_2)

		val ansattId = ARRANGOR_ANSATT_1.id
		val gjennomforing1Id = GJENNOMFORING_1.id
		val gjennomforing2Id = GJENNOMFORING_2.id

		val tilgangId1 = UUID.randomUUID()
		val tilgangId2 = UUID.randomUUID()

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = tilgangId1,
				ansattId = ansattId,
				gjennomforingId = gjennomforing1Id
			)
		)

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = tilgangId2,
				ansattId = ansattId,
				gjennomforingId = gjennomforing2Id
			)
		)

		repository.oppdaterGyldigTil(tilgangId2, ZonedDateTime.now().minusMinutes(1))

		val tilganger = repository.hentAktiveGjennomforingTilgangerForAnsatt(ansattId)

		tilganger.size shouldBe 1
		tilganger.any { it.gjennomforingId == gjennomforing1Id } shouldBe true
		tilganger.any { it.gjennomforingId == gjennomforing2Id } shouldBe false
	}

	test("oppdaterGyldigTil - skal sette gyldig_til") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertNavEnhet(NAV_ENHET_2)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangor(ARRANGOR_2)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_2)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)
		testRepository.insertGjennomforing(GJENNOMFORING_2)

		val ansattId = ARRANGOR_ANSATT_1.id
		val gjennomforingId = GJENNOMFORING_1.id
		val tilgangId = UUID.randomUUID()

		testRepository.insertArrangorAnsattGjennomforingTilgang(
			GJENNOMFORING_TILGANG_1.copy(
				id = tilgangId,
				ansattId = ansattId,
				gjennomforingId = gjennomforingId
			)
		)

		val stopTidspunkt = ZonedDateTime.now()

		repository.oppdaterGyldigTil(tilgangId, stopTidspunkt)

		val tilgang = repository.get(tilgangId)

		tilgang.gyldigTil shouldBeEqualTo stopTidspunkt
	}


})
