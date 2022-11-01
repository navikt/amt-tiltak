package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING1_DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*

class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	lateinit var testRepository: TestDataRepository

	lateinit var objectMapper: ObjectMapper

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		val template = NamedParameterJdbcTemplate(dataSource)
		rootLogger.level = Level.WARN

		testRepository = TestDataRepository(template)

		objectMapper = JsonUtils.objectMapper

		repository = EndringsmeldingRepository(
			template,
			objectMapper,
		)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("getByGjennomforing - en endringsmelding - henter endringsmelding") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 1
		meldinger[0].status shouldBe Endringsmelding.Status.AKTIV
	}

	test("getByGjennomforing - inaktiv endringsmelding - returnerer alle") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1.copy(
			id = UUID.randomUUID(),
			status = Endringsmelding.Status.UTDATERT)
		)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 2
		meldinger.any {it.status == Endringsmelding.Status.UTDATERT } shouldBe true
		meldinger.any {it.status == Endringsmelding.Status.AKTIV } shouldBe true
	}

	test("getByDeltaker - henter endringsmelding") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_2)

		val meldinger = repository.getByDeltaker(DELTAKER_1.id)

		meldinger.size shouldBe 1
		meldinger[0].opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
	}

	test("markerSomUtfort - skal sette status til UTFORT og nav ansatt") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)

		repository.markerSomUtfort(ENDRINGSMELDING1_DELTAKER_1.id, NAV_ANSATT_1.id)

		val oppdatertMelding = repository.get(ENDRINGSMELDING1_DELTAKER_1.id)

		oppdatertMelding.status shouldBe Endringsmelding.Status.UTFORT
		oppdatertMelding.utfortAvNavAnsattId shouldBe NAV_ANSATT_1.id
		oppdatertMelding.utfortTidspunkt!! shouldBeCloseTo ZonedDateTime.now()
	}

	test("getAktive - skal returnere tom liste hvis ingen deltakerIder er sendt inn") {
		repository.getAktive(emptyList()) shouldBe emptyList()
	}

	test("getAktive - skal hente aktive endringsmeldinger for deltakere") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)

		testRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_2)

		val aktiveMeldinger = repository.getAktive(listOf(DELTAKER_1.id, DELTAKER_2.id))

		aktiveMeldinger shouldHaveSize 2
		aktiveMeldinger.any { it.deltakerId == DELTAKER_1.id && it.status == Endringsmelding.Status.AKTIV }
		aktiveMeldinger.any { it.deltakerId == DELTAKER_2.id && it.status == Endringsmelding.Status.AKTIV }
	}
})
